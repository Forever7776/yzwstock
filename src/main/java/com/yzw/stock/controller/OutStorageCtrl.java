package com.yzw.stock.controller;

import com.alibaba.fastjson.serializer.DoubleArraySerializer;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.log.Logger;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import com.yzw.stock.model.*;
import com.yzw.stock.service.OutStorageService;
import com.yzw.stock.validator.OutStroageValidator;
import com.yzw.system.model.Account;

import java.util.List;

/**
 * Created by liuyj on 2015/7/31.
 * 入库管理
 */
@ControllerBind(controllerKey = "/stock/outstorage",viewPath = UrlConfig.STOCK)
public class OutStorageCtrl extends EasyuiController<OutStorage> {
    private Logger logger= Logger.getLogger(this.getClass());
    OutStorageService service = new OutStorageService(this);

    public void list(){
        if (this.isSuperAdmin()) {
            renderJson(OutStorage.dao.listByDataGrid(getAid(), getDataGrid(), this.getFrom(OutStorage.dao.tableName)));
        } else {
            renderJson(OutStorage.dao.listByDataGrid(this.user.getId(),getAid(),getDataGrid(), getFrom(OutStorage.dao.tableName)));
        }
    }

    @Before(value = { OutStroageValidator.class })
    public void add(){
        double totalFee = Double.parseDouble(getPara("total_fee"));
        Integer stockid = getParaToInt("child.stock_id");
        Integer out_type= getParaToInt("child.out_type");
        OutStorage outStorage = getModel(OutStorage.class,"outstorage").set("aid", getAid()).set("status", 1).set("insert_user_id",user.getId()).set("out_type",out_type).set("total_fee",totalFee).set("stock_id",stockid);
        boolean result = false;
        try {
            result = service.add(outStorage);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("outstorage@add error",e);
        }
        if(!result){
            String msg = "出库失败，该货物库存不足！";
            renderJsonResult(result,msg);
        }else{
            renderJsonResult(result);
        }
    }
    @Before(value = { OutStroageValidator.class })
    public void edit(){
        renderJsonResult(getModel().updateAndLastDate());
    }

    public void delete()
    {
        renderJsonResult(OutStorage.dao.update("status","-1",getPara("id")));
    }

    public void detail(){
        keepPara();
        render("outstorage.detail.html");
    }

    public void detailJson(){
        Integer outId = getParaToInt("outId");
        String product_name = getPara("yzwstock_product.name");
        keepPara();
        DataGrid<OutStorageDetail> detailDataGrid = new DataGrid<OutStorageDetail>();
        detailDataGrid.sortName = getPara("sort", "");
        detailDataGrid.sortOrder = getPara("order", "");
        detailDataGrid.page = getParaToInt("page", 1);
        detailDataGrid.total = getParaToInt("rows", 15);
        if(isSuperAdmin()){
            renderJson(OutStorageDetail.dao.listByDetailDataGrid(null,outId,product_name, detailDataGrid, getFrom(OutStorageDetail.dao.tableName)));
        }else{
            renderJson(OutStorageDetail.dao.listByDetailDataGrid(this.user.getId(),outId,product_name, detailDataGrid, getFrom(OutStorageDetail.dao.tableName)));
        }

    }

    /**
     * 审批入口
     */
    public void review(){
        OutStorage outStorage = getModel(OutStorage.class,"outStorage");
        outStorage.setDate("review_date");
        outStorage.set("review_user",user.getName());
        outStorage.set("review_user_id",user.getId());
        boolean result = false;
        try{
            result = service.review(outStorage);
        }catch (Exception e){
            logger.error("outStorageCtrl@review error");
        }
        renderJsonResult(result);
    }


    public void check(){
        OutStorage outStorage = getModel(OutStorage.class,"outStorage");
        outStorage.setDate("check_date");
        outStorage.set("check_user", user.getName());
        outStorage.set("check_user_id", user.getId());
        boolean result =false;
        try{
            result = service.checkProcess(outStorage);
        }catch (Exception e){
            logger.error("outStorageCtrl@check error",e);
        }
        renderJsonResult(result);
    }

    public void outStorageNumscheck(){
        boolean result =false;
        Long nums = 0L;
        try{
            nums = service.getStockNums(this.getAid(), this.getParaToInt("productid"), this.getParaToInt("stockid"));
        }catch (Exception e){
            logger.error("outStorageCtrl@outStorageNumscheck error",e);
        }
        String msg = "";
        if(nums>=this.getParaToInt("inputnums",0)){
            result = true;
            msg="库存满足现出库数";
        }else{
            result = false;
            msg="库存不足，最大可出库数为："+nums;
        }

        renderJsonResult(result,msg);
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

    public void loadOutProductStockData(){
        DataGrid<StockProduct> dataGrid = new DataGrid<StockProduct>();
        dataGrid.sortName = getPara("sort", "");
        dataGrid.sortOrder = getPara("order", "");
        dataGrid.page = getParaToInt("page", 1);
        dataGrid.total = getParaToInt("rows", 15);
        Form f = getFrom(StockProduct.dao.tableName);
        OutStorageDetail outStorageDetail = OutStorageDetail.dao.findById(this.getParaToInt("outDetailId"));
        if(outStorageDetail!=null){
            f.addFrom(f,"outstorage_id-i",outStorageDetail.getInt("outstorage_id")+"");
            f.addFrom(f,"product_id-i",outStorageDetail.getInt("product_id")+"");
            renderJson(StockProduct.dao.queryOutProductDetailGrid(dataGrid, f));
        }else{
            renderJson500();
        }
    }

    public void print(){
        //获得数据
        OutStorage outStorage = OutStorage.dao.getOutStorageData(this.getParaToInt("outid"));
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account  account=Account.dao.findFirst("SELECT * FROM  admin_account where id=?",outStorage.get("aid"));
        service.outStorageTableTempateExport(1, path, outStorage,account, this.getResponse(), null);
        this.renderNull();
    }

    public void getOutAddress(){
        OutStorage outStorage = OutStorage.dao.findById(this.getPara("outid"));
        if(outStorage!=null){
            if(outStorage.get("out_address_id")!=null){
                OutStorageAddress address = OutStorageAddress.dao.findById(outStorage.get("out_address_id"));
                this.setAttr("address",address);
            }else if(outStorage.get("customer_id")!=null){
                Customer customer = Customer.dao.findById(outStorage.get("customer_id"));
                this.setAttr("customer",customer);
            }
        }

        this.setAttr("outStorage",outStorage);
        renderJson();
    }

    public void printOutStorageChoose(){
        //获得选择时间数据
        String start=this.getPara("start");
        String end=this.getPara("end");
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account account=Account.dao.findById(getAid());
        List<OutStorage> outStorageList=null;
        if(start.equals(end)){
            outStorageList=OutStorage.dao.find("SELECT sum(t.product_num) as nums,t.name,t.spec,t.product_price,t.unit,t.stock_name,t.insert_date from(select yzwstock_out_storage_detail.product_num,yzwstock_product.name," +
                    "yzwstock_product.spec,yzwstock_out_storage_detail.product_price,yzwstock_product.unit,yzwstock_stock.name as stock_name ,yzwstock_out_storage_detail.insert_date from yzwstock_out_storage_detail " +
                    "left join yzwstock_product on yzwstock_product.id = yzwstock_out_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id  where yzwstock_stock.aid=? " +
                    "and yzwstock_out_storage_detail.insert_date  like '%"+start+"%') t GROUP BY name ,spec order by insert_date desc",getAid());
        }else{
            outStorageList=OutStorage.dao.find("SELECT sum(t.product_num) as nums,t.name,t.spec,t.product_price,t.unit,t.stock_name,t.insert_date from(select yzwstock_out_storage_detail.product_num,yzwstock_product.name," +
                    "yzwstock_product.spec,yzwstock_out_storage_detail.product_price,yzwstock_product.unit,yzwstock_stock.name as stock_name ,yzwstock_out_storage_detail.insert_date from yzwstock_out_storage_detail " +
                    "left join yzwstock_product on yzwstock_product.id = yzwstock_out_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id  where yzwstock_stock.aid=? " +
                    "and yzwstock_out_storage_detail.insert_date >=? and yzwstock_out_storage_detail.insert_date <=?) t GROUP BY name ,spec order by insert_date desc",getAid(),start,end);
        }

        service.OutStorageChooseTableTempateExport(1, path, outStorageList, account, this.getResponse(), null);
        this.renderNull();
    }

}
