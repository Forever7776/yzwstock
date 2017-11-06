package com.yzw.stock.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;

import com.jfinal.log.Logger;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.stock.model.*;
import com.yzw.stock.service.MoveStorageService;
import com.yzw.stock.validator.MoveStroageValidator;
import com.yzw.system.model.Account;

import java.util.List;


/**
 * Created by ytc on 2015/8/24.
 * 入库管理
 */
@ControllerBind(controllerKey = "/stock/movestorage",viewPath = UrlConfig.STOCK)
public class MoveStorageCtr extends EasyuiController<MoveStorage>{
    private Logger logger = Logger.getLogger(this.getClass());

    MoveStorageService service = new MoveStorageService(this);
    public void list(){
        if(isSuperAdmin()){
            renderJson(MoveStorage.dao.listByDataGrid(getAid(), getDataGrid(), this.getFrom(MoveStorage.dao.tableName)));
        }else {
            renderJson(MoveStorage.dao.listByDataGrid(this.user.getId(),getAid(), getDataGrid(), getFrom(MoveStorage.dao.tableName)));
        }
    }

    @Before(value = { MoveStroageValidator.class })
    public void add(){
        MoveStorage movestorage = getModel(MoveStorage.class, "movestorage").set("aid", getAid()).set("status", 1);
        OutStorage  outstorage  = getModel(OutStorage.class, "outstorage").set("aid", getAid()).set("status", 1).set("insert_user_id",user.getId()).set("out_type",2);
        InStorage   inStorage   = getModel(InStorage.class,"instorage").set("aid", getAid()).set("status", 1).set("insert_user_id",user.getId()).set("type",2);
        boolean result = false;
        try {
            result = service.add(movestorage,outstorage,inStorage);
        }catch (Exception e){
            logger.error("MovaeStorageCtrl@add error",e);
        }
        renderJsonResult(result);
    }

    @Before(value = { MoveStroageValidator.class })
    public void edit(){
        renderJsonResult(getModel().updateAndLastDate());
    }

    public void delete() {
        MoveStorage moveStorage = MoveStorage.dao.findById(getPara("id"));
        String outstorage_id = moveStorage.get("outstorage_id").toString();
        String instorage_id  = moveStorage.get("instorage_id").toString();
        if(outstorage_id !=null && instorage_id != null){
            try{
                moveStorage.dao.falseDetele(getPara("id"));
                OutStorage.dao.falseDetele(outstorage_id);
                InStorage.dao.falseDetele(instorage_id);
            }catch (Exception e){
                logger.error("MovaeStorageCtrl@delete error",e);
            }
            renderJsonResult(true);
        }else{
            renderJsonResult(false);
        }
    }

    public void review(){
        MoveStorage moveStorage = getModel(MoveStorage.class, "moveStorage");;
        OutStorage outStorage = OutStorage.dao.findById(moveStorage.getInt("outstorage_id"));
        InStorage inStorage = InStorage.dao.findById(moveStorage.getInt("instorage_id"));
        outStorage.setDate("review_date");
        outStorage.set("review_user", user.getName());
        outStorage.set("review_user_id",user.getId());
        inStorage.setDate("review_date");
        inStorage.set("review_user",user.getName());
        inStorage.set("review_user_id",user.getId());
        int status = getParaToInt("moveStorage.review_status");
        String text = getPara("moveStorage.review_text");
        boolean result = false;
        try{
            result = service.review(status, text, moveStorage, outStorage, inStorage);
        }catch (Exception e){
            logger.error("MovaeStorageCtrl@review error");
        }
        renderJsonResult(result);
    }

    public void check(){
        MoveStorage moveStorage = getModel(MoveStorage.class, "moveStorage");;
        OutStorage outStorage = OutStorage.dao.findById(moveStorage.getInt("outstorage_id"));
        InStorage inStorage = InStorage.dao.findById(moveStorage.getInt("instorage_id"));
        outStorage.setDate("review_date");
        outStorage.set("check_user", user.getName());
        outStorage.set("check_user_id", user.getId());
        inStorage.setDate("check_date");
        inStorage.set("check_user", user.getName());
        inStorage.set("check_user_id", user.getId());
        moveStorage.setDate("review_date");

        boolean result = false;
        try {
            result = service.check(outStorage, moveStorage, inStorage);
        } catch (Exception e) {
            logger.error("InStorageCtrl@check error", e);
        }
        renderJsonResult(result);
    }

    public void detailJson(){
        renderJson(MoveStorage.dao.detailJsonDataGrid(getAid(), getParaToInt("id"), getDataGrid(), this.getFrom(MoveStorage.dao.tableName)));
    }

    public void printMoveStorageChoose(){
        //获得选择时间数据
        String start=this.getPara("start");
        String end=this.getPara("end");
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account account= Account.dao.findById(getAid());
        List<MoveStorage> moveStorageList=null;
        if(start.equals(end)){
            moveStorageList=MoveStorage.dao.find("\n" +
                    "SELECT SUM(t.product_num) as nums,t.name, t.spec,t.product_price,t.unit,t.stock_name,t.insert_date from(\n" +
                    "select yzwstock_product.name,yzwstock_out_storage_detail.product_num,yzwstock_product.spec,yzwstock_out_storage_detail.product_price,yzwstock_product.unit,yzwstock_stock.name as stock_name,m.insert_date \n" +
                    "from yzwstock_movestore m \n" +
                    "left join yzwstock_out_storage on yzwstock_out_storage.id= m.outstorage_id  \n" +
                    "left join  yzwstock_out_storage_detail on yzwstock_out_storage_detail.outstorage_id=yzwstock_out_storage.id  \n" +
                    "left join yzwstock_product  on yzwstock_product.id=yzwstock_out_storage_detail.product_id \n" +
                    "left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id \n" +
                    "where ( m.aid=?  and yzwstock_out_storage.out_type=8 ) and m.insert_date like '%"+start+"%') t GROUP BY t.name,t.spec ORDER BY t.insert_date",getAid());
        }else{
            moveStorageList=MoveStorage.dao.find("\n" +
                    "SELECT SUM(t.product_num) as nums,t.name, t.spec,t.product_price,t.unit,t.stock_name,t.insert_date from(\n" +
                    "select yzwstock_product.name,yzwstock_out_storage_detail.product_num,yzwstock_product.spec,yzwstock_out_storage_detail.product_price,yzwstock_product.unit,yzwstock_stock.name as stock_name,m.insert_date \n" +
                    "from yzwstock_movestore m \n" +
                    "left join yzwstock_out_storage on yzwstock_out_storage.id= m.outstorage_id  \n" +
                    "left join  yzwstock_out_storage_detail on yzwstock_out_storage_detail.outstorage_id=yzwstock_out_storage.id  \n" +
                    "left join yzwstock_product  on yzwstock_product.id=yzwstock_out_storage_detail.product_id \n" +
                    "left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id \n" +
                    "where ( m.aid=?  and yzwstock_out_storage.out_type= 8 ) and m.insert_date >=? and m.insert_date<=?) t GROUP BY t.name,t.spec ORDER BY t.insert_date",getAid(),start,end);
        }

        service.MoveStorageChooseTableTempateExport(1, path, moveStorageList, account, this.getResponse(), null);
        this.renderNull();
    }

}
