package com.yzw.stock.controller;

import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.stock.model.Product;
import com.yzw.stock.model.StockProduct;

/**产品库存查询
 * Created by liuyj on 2015/7/27.
 */
@ControllerBind(controllerKey = "/stock/replenish",viewPath = UrlConfig.STOCK)
public class StockReplenishCtrl extends EasyuiController<Product>{
    public void index(){
        this.render("stock.replenish.html");
    }
    /**
     * 查询产品对应的库存
     */
    public void list(){
        String warnning_category_name=getPara("warnning_category_name");
        String warnning_stock_name=getPara("warnning_stock_name");
        if(this.isSuperAdmin()){
            renderJson(StockProduct.dao.listByReplenish(null,this.getAid(), 1,warnning_category_name,warnning_stock_name));
        }else{
            renderJson(StockProduct.dao.listByReplenish(this.user.getId(),null, 1,warnning_category_name,warnning_stock_name));
        }
    }
}