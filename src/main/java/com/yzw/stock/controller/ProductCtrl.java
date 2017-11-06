package com.yzw.stock.controller;

import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.stock.model.Product;
import com.yzw.stock.validator.ProductValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by luomhy on 2015/7/23.
 * 货物controller
 */
@ControllerBind(controllerKey = "/stock/product",viewPath = UrlConfig.STOCK)
public class ProductCtrl extends EasyuiController<Product> {
    public void tree()
    {
        Integer passId = getParaToInt("passId");
        renderJson(Product.dao.getTree(getAid(), passId));
    }
    public void list()
    {
        renderJson(Product.dao.listByDataGrid(getAid(), getDataGrid(), getFrom(Product.dao.tableName)));
    }

    public void listbyProduct(){
        renderJson(Product.dao.listbyProduct(getAid()));
    }

    @Before(value = { ProductValidator.class })
    public void add(){
        Product product = getModel().set("aid",getAid()).set("status",1).setDate("last_date");
        if(StringUtils.isNotBlank(product.getName())) {
            product.set("pinyin", PinyinHelper.getShortPinyin(product.getName()));
        }
        renderJsonResult(product.saveAndInsertDate());
    }

    @Override
    @Before(value = { ProductValidator.class })
    public void edit(){
        Product product = getModel();
        if(StringUtils.isNotBlank(product.getName())) {
            product.set("pinyin", PinyinHelper.getShortPinyin(product.getName()));
        }
        renderJsonResult(product.updateAndLastDate());
    }

    public void delete()
    {

        renderJsonResult(Product.dao.falseDetele(getPara("id")));
    }
}
