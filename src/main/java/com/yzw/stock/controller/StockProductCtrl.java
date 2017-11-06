package com.yzw.stock.controller;

import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.stock.model.StockProduct;

/**
 * Created by liuyj on 2015/9/13.
 */
@ControllerBind(controllerKey = "/stock/stockProduct",viewPath = UrlConfig.STOCK)
public class StockProductCtrl extends EasyuiController<StockProduct> {

    public void loadProductStockData(){
        keepPara();
        String search_product=getPara("searchProduct");
        renderJson(StockProduct.dao.queryStockData(getDataGrid(),getAid(),this.getParaToInt("stockid"), getFrom(StockProduct.dao.tableName),search_product));
    }
}
