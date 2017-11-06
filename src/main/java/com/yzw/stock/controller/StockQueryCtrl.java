package com.yzw.stock.controller;

import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import com.yzw.stock.model.Product;
import com.yzw.stock.model.StockProduct;

import java.util.List;

/**
 * 产品库存查询
 * Created by liuyj on 2015/7/27.
 */
@ControllerBind(controllerKey = "/stock/stockquery", viewPath = UrlConfig.STOCK)
public class StockQueryCtrl extends EasyuiController<Product> {
    public void index() {
        this.render("stock.query.html");
    }

    /**
     * 查询产品对应的库存
     */
    public void list() {

        if (this.isSuperAdmin()) {
            DataGrid<Product> productDataGrid = Product.dao.productStockQueryByGrid(this.getAid(), getDataGrid(), getFrom(Product.dao.tableName));
            for (Product product : productDataGrid.getRows()) {
                long enablenums = StockProduct.getProdctStockNumsWithExcludelocking(0, product.getInt("id"));
                product.put("lockingnums", product.getLong("stock_nums") - enablenums);
                product.put("enablenums", enablenums);
            }
            renderJson(productDataGrid);
        } else {
            DataGrid<Product> productDataGrid = Product.dao.productStockQueryByGrid(this.getAid(), this.user.getId(), getDataGrid(), getFrom(Product.dao.tableName));
            for (Product product : productDataGrid.getRows()) {
                if (product.getInt("stockid") == null) {
                    product.put("lockingnums", 0);
                    product.put("enablenums", 0);
                } else {
                    long enablenums = StockProduct.getProdctStockNumsWithExcludelocking(product.getInt("stockid"), product.getInt("id"));
                    product.put("lockingnums", product.getLong("stock_nums") - enablenums);
                    product.put("enablenums", enablenums);
                }

            }
            renderJson(productDataGrid);
        }
    }


    public void detailList() {
        this.setAttr("productid", this.getParaToInt("productid"));
        this.render("stock.detail.list.query.html");
    }

    public void stockDetailListQuery() {
        if (this.isSuperAdmin()) {
            renderJson(Product.dao.stockDetailQueryByGrid(this.getAid(), this.getParaToInt("productid"), this.getParaToInt("stockid"), this.getPara("code"), getDataGrid(), getFrom(Product.dao.tableName)));
        } else {
            renderJson(Product.dao.stockDetailQueryByGrid(this.user.getId(), this.getParaToInt("productid"), this.getParaToInt("stockid"), this.getPara("code"), getDataGrid(), getFrom(Product.dao.tableName)));
        }

    }

    public void getstockList() {
        Form form = getFrom(Product.dao.tableName);
        form.addFrom(form, "st.name-*", this.getPara("yzwstock_stock.name"));
        if (this.isSuperAdmin()) {
            DataGrid<Product> dataGrid = Product.dao.getstockListsql(this.getAid(), null, this.getParaToInt("productid"), getDataGrid(), form);
            for (Product product : dataGrid.getRows()) {
                long enablenums = StockProduct.getProdctStockNumsWithExcludelocking(product.getInt("stockid"), this.getParaToInt("productid"));
                product.put("lockingnums", product.getLong("stocksum") - enablenums);
                product.put("enablenums", enablenums);
            }
            renderJson(dataGrid);
        } else {
            DataGrid<Product> dataGrid = Product.dao.getstockListsql(this.getAid(), this.user.getId(), this.getParaToInt("productid"), getDataGrid(), form);
            for (Product product : dataGrid.getRows()) {
                long enablenums = StockProduct.getProdctStockNumsWithExcludelocking(product.getInt("stockid"), this.getParaToInt("productid"));
                product.put("lockingnums", product.getLong("stocksum") - enablenums);
                product.put("enablenums", enablenums);
            }
            renderJson(dataGrid);
        }

    }

    public List<StockProduct> Elist() {
        String sql = "select yzwstock_product.*,IFNULL(sp.stock_nums,0) as stock_nums,yzwstock_product_category.name as category_name  from  yzwstock_product left join  (select product_id,count(*) as stock_nums from yzwstock_stock_product where status = 1  group by product_id) sp on yzwstock_product.id = sp.product_id left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id";
        List<StockProduct> st = StockProduct.dao.find(sql);
        return st;

    }
}