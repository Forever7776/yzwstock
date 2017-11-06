package com.yzw.stock.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.Consts;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ShiroExt;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.stock.model.InStorage;
import com.yzw.stock.model.InStorageDetail;
import com.yzw.stock.model.Stock;
import com.yzw.stock.model.Warning;
import com.yzw.stock.validator.StockValidator;
import com.yzw.system.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luomhy on 2015/7/24.
 */
@ControllerBind(controllerKey = "/stock/manager",viewPath = UrlConfig.STOCK)
public class StockCtrl extends EasyuiController<Stock> {

    public void tree()
    {
        Integer pid = getParaToInt("id");
        Integer passId = getParaToInt("passId");
        Integer admin=user.getInt("id");
        if (this.isSuperAdmin()) {
            renderJson(Stock.dao.getTree(getAid(), pid, passId));
        }else{
            renderJson(Stock.dao.getTree(getAid(), pid, passId, admin));
        }

    }



    public void list(){
        String storage_name = getPara("storage_name");
        if(storage_name!=null){
            renderJson(Stock.dao.list(getAid(), storage_name));
        }else {
            renderJson(Stock.dao.list(getAid()));
        }
    }

    @Before(value = { StockValidator.class })
    public void add()
    {
        renderJsonResult(getModel().set("aid", getAid()).set("insert_user_id", user.getId()).set("status", 1).saveAndInsertDate());
    }

    @Override
    @Before(value = { StockValidator.class })
    public void edit()
    {
        renderJsonResult(getModel(Stock.class, "stock").updateAndLastDate());
    }


    public void delete()
    {
        renderJsonResult(Stock.dao.falseDetele(getPara("id")));
    }

    public void total(){
        Integer  is_total=getParaToInt("is_total");
        if (is_total==0){
            is_total=1;
        }else if(is_total==1){
            is_total=0;
        }
        renderJsonResult(Stock.dao.TotalStock(getParaToInt("id"), is_total));
    }
    public void detail(){
        keepPara();
        render("product.warning.html");
    }
    public void detailJson(){
        Integer inId = getParaToInt("inId");
        String search=getPara("yzwstock_product.name");
        keepPara();
        DataGrid<Warning> detailDataGrid = new DataGrid<Warning>();
        detailDataGrid.sortName = getPara("sort", "");
        detailDataGrid.sortOrder = getPara("order", "");
        detailDataGrid.page = getParaToInt("page", 1);
        detailDataGrid.total = getParaToInt("rows", 15);
        if(search!=null){
            renderJson(Warning.dao.listByDetailDataGrid(getAid(), inId, search, detailDataGrid, getFrom(Warning.dao.tableName)));
        }else {
            renderJson(Warning.dao.listByDetailDataGrid(getAid(), inId, detailDataGrid, getFrom(Warning.dao.tableName)));
        }
    }

    public void addWar(){
        Integer stockid = getParaToInt("stockid");
        Integer productid = getParaToInt("productid");
        renderJsonResult(getModel(Warning.class,"warning").set("stockid",stockid).set("productid",productid).saveAndInsertDate());
    }

    public void etidWar(){
        renderJsonResult(getModel(Warning.class,"warning").updateAndLastDate());
    }

    public void editWar()
    {
        renderJsonResult(getModel(Warning.class, "warning").updateAndLastDate());
    }

    public void map(){
        this.setAttr("address",this.getPara("address"));
        render("map.html");
    }
}
