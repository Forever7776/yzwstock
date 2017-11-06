package com.yzw.stock.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.util.serial.GenerateSerialNo;
import com.yzw.stock.model.Customer;
import com.yzw.stock.model.Supplier;
import com.yzw.stock.validator.SupplierValidator;

/**
 * Created by liuyj on 2015/7/27.
 */
@ControllerBind(controllerKey = "/stock/customer",viewPath = UrlConfig.STOCK)
public class CustomerCtrl extends EasyuiController<Customer> {
    public void tree(){
        Integer passId = getParaToInt("passId");
        renderJson(Customer.dao.getTree(getAid(), passId));
    }
    public void list(){
        renderJson(Customer.dao.listByDataGrid(this.getAid(),getDataGrid(), getFrom(Customer.dao.tableName)));
    }
    public void add(){
        renderJsonResult(getModel(Customer.class, "customer").set("aid", getAid()).set("type","1").set("code", GenerateSerialNo.createSerialSupplierCode()).set("status",1).saveAndInsertDate());
    }

    public void delete(){
        renderJsonResult(Customer.dao.falseDetele(getPara("id")));
    }
    public void batchDelete(){
        renderJsonResult(Customer.dao.batchDelete(getPara("ids")));
    }

    public void freeze()
    {
        int to_status = 1;
        if(getParaToInt("status")==1){
            to_status = 2;
        }else if (getParaToInt("status")==2){
            to_status = 1;
        }
        renderJsonResult(Customer.dao.changeStaus(getParaToInt("id"), to_status));
    }

    @Override
    public void edit(){
        renderJsonResult(getModel().updateAndLastDate());
    }
}
