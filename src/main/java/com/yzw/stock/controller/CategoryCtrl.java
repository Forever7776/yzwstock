package com.yzw.stock.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.stock.model.ProductCategory;
import com.yzw.stock.validator.CategoryValidator;

import javax.xml.ws.Action;

/**
 * Created by luomhy on 2015/7/23.
 */
@ControllerBind(controllerKey = "/stock/category",viewPath = UrlConfig.STOCK)
public class CategoryCtrl extends Controller<ProductCategory> {

    public void tree()
    {
        Integer pid = getParaToInt("id");
        Integer passId = getParaToInt("passId");
        renderJson(ProductCategory.dao.getTree(getAid(), pid, passId));
    }

    @Before(value = { CategoryValidator.class })
    public void add(){
        ProductCategory category=getModel(ProductCategory.class, "category");
        boolean result=category.set("aid",getAid()).saveAndInsertDate();
        renderJsonResult(result);
    }

    @Before(value = { CategoryValidator.class })
    public void edit(){
        ProductCategory category=getModel(ProductCategory.class, "category");
        if (category.getId() == category.getPid()) renderJsonError("父节点不能为自己");
        else if (ProductCategory.dao.pidIsChild(category.getId(), category.getPid())) renderJsonError("父节点不能为子节点");
        else
        {
            renderJsonResult(category.update());
        }
    }
    public void list()
    {
        renderJson(ProductCategory.dao.listOrderByInsertDate("where aid=?",getAid()));
    }

    public void delete(){
        renderJsonResult(ProductCategory.dao.deleteByIdAndPid(getParaToInt("id")));
    }
}
