package com.yzw.stock.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.util.serial.GenerateSerialNo;
import com.yzw.stock.model.Product;
import com.yzw.stock.model.Supplier;
import com.yzw.stock.validator.ProductValidator;
import com.yzw.stock.validator.SupplierValidator;
import com.yzw.system.model.User;
import com.yzw.system.validator.UserValidator;

/**
 * Created by liuyj on 2015/7/24.
 */
@ControllerBind(controllerKey = "/stock/supplier",viewPath = UrlConfig.STOCK)
public class SupplierCtrl extends EasyuiController<Supplier> {

    public void tree(){
        Integer passId = getParaToInt("passId");
        renderJson(Supplier.dao.getTree(getAid(), passId));
    }
    public void list(){
        renderJson(Supplier.dao.listByDataGrid(this.getAid(),getDataGrid(), getFrom(Supplier.dao.tableName)));
    }
    public void add(){
        renderJsonResult(getModel(Supplier.class, "supplier").set("aid", getAid()).set("code", GenerateSerialNo.createSerialSupplierCode()).set("status",1).saveAndInsertDate());
    }

    public void delete(){
        renderJsonResult(Supplier.dao.falseDetele(getPara("id")));
    }
    public void batchDelete(){
        renderJsonResult(Supplier.dao.batchDelete(getPara("ids")));
    }

    public void freeze()
    {
        int to_status = 1;
        if(getParaToInt("status")==1){
            to_status = 2;
        }else if (getParaToInt("status")==2){
            to_status = 1;
        }
        renderJsonResult(Supplier.dao.changeStaus(getParaToInt("id"), to_status));
    }

    @Override
    @Before(value = {SupplierValidator.class })
    public void edit(){
        renderJsonResult(getModel().updateAndLastDate());
    }

}
