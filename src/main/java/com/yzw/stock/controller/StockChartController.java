package com.yzw.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.stock.model.Product;
import com.yzw.stock.model.StockProduct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@ControllerBind(controllerKey = "/stock/stockchart" ,viewPath= UrlConfig.STOCK)
public class StockChartController extends EasyuiController<Product>
{

    public void echartsList() {
        String pageSize = getPara("pageSize");//10
        String currentPage = getPara("currentPage");//1
        List<Product> list = new ArrayList<Product>();
        List<Product> productData = Product.dao.find("select yzwstock_product.*,IFNULL(sp.stock_nums,0) as stock_nums,yzwstock_product_category.name as category_name from  yzwstock_product left join  (select product_id,count(*) as stock_nums from yzwstock_stock_product " +
                "where status = 1  group by product_id) sp on yzwstock_product.id = sp.product_id left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id");
        List specnames = new LinkedList();
        List stock_nums = new LinkedList();
        List enablenums = new LinkedList();
        List freeze_nums = new LinkedList();
        int beginIndex = (Integer.parseInt(currentPage) - 1) * Integer.parseInt(pageSize);
        int endIndex = beginIndex + Integer.parseInt(pageSize) - 1;
        int totalPage = 0;
        if(productData.size() % Integer.parseInt(pageSize) == 0){
            totalPage = productData.size() / Integer.parseInt(pageSize);
        }else {
            totalPage = productData.size() / Integer.parseInt(pageSize) + 1;
        if(endIndex >= productData.size()){
            endIndex = productData.size() - 1;
        }
        list = productData.subList(beginIndex, endIndex);
        for (Product product : list) {
            long enablenum = StockProduct.getProdctStockNumsWithExcludelocking(0, product.getInt("id"));
            product.put("lockingnums", product.getLong("stock_nums") - enablenum);
            product.put("enablenums", enablenum);
            Long stock_num = product.getLong("stock_nums");
            long freeze_num = stock_num - enablenum;
            String specname = product.get("name") + "" + product.get("spec");
            specnames.add(specname);
            stock_nums.add(stock_num);
            enablenums.add(enablenum);
            freeze_nums.add(freeze_num);
        }
        JSONObject obj = new JSONObject();
        obj.put("specnames", specnames);
        obj.put("stock_nums", stock_nums);
        obj.put("enablenums", enablenums);
        obj.put("freeze_nums", freeze_nums);
        obj.put("totalPage",totalPage);
        renderJson(obj);
    }
}



}
