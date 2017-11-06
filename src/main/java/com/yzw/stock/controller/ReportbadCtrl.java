package com.yzw.stock.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.log.Logger;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.util.serial.GenerateSerialNo;
import com.yzw.stock.model.OutStorage;
import com.yzw.stock.model.Reportbad;
import com.yzw.stock.service.ReportbadService;
import com.yzw.stock.validator.ReportbadValidator;
import com.yzw.system.model.Account;

import java.util.List;

/**
 * Created by liuyj on 2015/8/14.
 */
@ControllerBind(controllerKey = "/stock/reportbad", viewPath = UrlConfig.STOCK)
public class ReportbadCtrl extends EasyuiController<Reportbad> {
    private static Logger logger = Logger.getLogger(ReportbadCtrl.class);
    ReportbadService service = new ReportbadService(this);
    public void list(){
        if(isSuperAdmin()){
            renderJson(Reportbad.dao.listByDataGrid(getAid(), getDataGrid(), this.getFrom(Reportbad.dao.tableName)));
        }else {
            renderJson(Reportbad.dao.listByDataGrid(this.user.getId(),getAid(), getDataGrid(), getFrom(Reportbad.dao.tableName)));
        }
    }

    @Before(value = {ReportbadValidator.class })
    public void add(){
        Reportbad reportbad = getModel(Reportbad.class,"reportbad").set("aid", getAid()).set("status", 1).set("insert_user_id",user.getId()).set("code", GenerateSerialNo.createSerialReportBadCode());
        boolean result = false;
        try {
            result = service.add(reportbad, user);
        }catch (Exception e){
            logger.error("outstorage@add error",e);
        }
        renderJsonResult(result);
    }

    /**
     * 审批入口
     */
    public void review(){
        Reportbad reportbad = getModel(Reportbad.class, "reportbad");
        OutStorage outStorage = OutStorage.dao.findById(reportbad.getInt("outstorage_id"));
        reportbad.setDate("review_date");
        reportbad.set("review_user",user.getName());
        reportbad.set("review_user_id",user.getId());
        outStorage.setDate("review_date");
        outStorage.set("review_user",user.getName());
        outStorage.set("review_user_id",user.getId());
        boolean result = false;
        try{
            result = service.review(reportbad, outStorage);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("ReportbadCtrl@review error");
        }
        renderJsonResult(result);
    }

    public void delete()
    {
        Reportbad reportbad = Reportbad.dao.findById(getPara("id"));
        String outstorageId= reportbad.get("outstorage_id").toString();
        if(reportbad != null && outstorageId != null){
            try{
                reportbad.dao.falseDetele(getPara("id"));
                OutStorage.dao.falseDetele(outstorageId);
                OutStorage.dao.update("status",-1,outstorageId);
            }catch (Exception e){
                e.printStackTrace();
                System.out.print("ReportbadCtrl@delete erro");
            }
            renderJsonResult(true);
        }
        else{
            renderJsonResult(false);
        }
    }
    /**
     *扫码出库
     */
    @Clear
    public void scanCodeOsutStroage(){
            int resultCode = 0;
            try {
                resultCode = service.ScanCodeOutStroageProcess(this.getAid(),this.getPara("pcode"),this.getParaToInt("outid"));
            }catch (Exception e){
                logger.error("outStorageCtrl@ScanCodeOutStroage error",e);
            }
            String msg = "";
            boolean result = false;
            switch (resultCode){
            case 0:
                result = false;
                msg="出库失败，系统异常！";
                break;
            case 1:
                result =true;
                msg="该货物出库成功！";
                break;
            case -1:
                result = false;
                msg="出库失败，该货物数据不存在！";
                break;
            case -2:
                result = false;
                msg="出库失败，系统异常！";
                break;
            case -3:
                result = false;
                msg="出库失败，该货物已扫码出库，无需重复扫码！";
                break;
            case -4:
                result = false;
                msg="出库失败，系统异常！";
                break;
            case -5:
                result = false;
                msg="出库失败，该货物不再出库单中！";
                break;
            case -6:
                result = false;
                msg="出库失败，该货物出库扫码数已满！";
                break;
        }
        logger.info("outStorageCtrl@ScanCodeOutStroage:" + msg + ",Code：" + resultCode);
        renderJsonResult(result,msg);

    }

    public void print(){
        Reportbad reportbad =Reportbad.dao.findById(this.getPara("id"));
        //获得数据
        OutStorage outStorage = OutStorage.dao.getOutStorageData(reportbad.getInt("outstorage_id"));
        Account account=Account.dao.findFirst("SELECT * FROM  admin_account where id=?", outStorage.get("aid"));
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        service.ReportbadTableTempateExport(1, path, outStorage, account,this.getResponse(), null);
        this.renderNull();
    }

    public void detaByDataGrid(){
        if(isSuperAdmin()) {
            renderJson(Reportbad.dao.detaByDataGrid(getAid(), null, getParaToInt("id"), getDataGrid(), getFrom(Reportbad.dao.tableName)));
        }else{
            renderJson(Reportbad.dao.detaByDataGrid(getAid(), this.user.getId(), getParaToInt("id"), getDataGrid(), getFrom(Reportbad.dao.tableName)));

        }
    }


    public void printReportbadChoose(){
        //获得选择时间数据
        String start=this.getPara("start");
        String end=this.getPara("end");
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account account=Account.dao.findById(getAid());
        List<Reportbad> reportbadList=null;
        if(start.equals(end)){
            reportbadList=Reportbad.dao.find("SELECT sum(s.product_num) as nums,s.name,s.spec,s.unit,s.stock_name ,s.insert_date from(select yzwstock_product.name,yzwstock_out_storage_detail.product_num,yzwstock_product.spec," +
                    "yzwstock_stock.name as stock_name ,yzwstock_product.unit ,t.insert_date from yzwstock_reportbad t left join yzwstock_out_storage on yzwstock_out_storage.id = t.outstorage_id  " +
                    "left join  yzwstock_out_storage_detail on  yzwstock_out_storage_detail.outstorage_id=yzwstock_out_storage.id left join yzwstock_product  on yzwstock_product.id=yzwstock_out_storage_detail.product_id " +
                    "left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id where 1=1  and yzwstock_product.aid=?  and t.insert_date like '%"+start+"%' ) s GROUP BY s.name,s.spec",getAid());
        }else{
            reportbadList=Reportbad.dao.find("SELECT sum(s.product_num) as nums,s.name,s.spec,s.unit,s.stock_name ,s.insert_date from(select yzwstock_product.name,yzwstock_out_storage_detail.product_num,yzwstock_product.spec," +
                    "yzwstock_stock.name as stock_name ,yzwstock_product.unit ,t.insert_date from yzwstock_reportbad t left join yzwstock_out_storage on yzwstock_out_storage.id = t.outstorage_id  " +
                    "left join  yzwstock_out_storage_detail on  yzwstock_out_storage_detail.outstorage_id=yzwstock_out_storage.id left join yzwstock_product  on yzwstock_product.id=yzwstock_out_storage_detail.product_id " +
                    "left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id where 1=1  and yzwstock_product.aid=?  and t.insert_date >=? and t.insert_date <=?) s GROUP BY s.name,s.spec",getAid(),start,end);
        }

        service.ReportbadChooseTableTempateExport(1, path, reportbadList, account, this.getResponse(), null);
        this.renderNull();
    }
}
