package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import com.yzw.base.model.easyui.Tree;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luomhy on 2015/7/23.
 */

@TableBind(tableName = "yzwstock_product")
public class Product extends EasyuiModel<Product> {
    public static Product dao = new Product();

    public DataGrid<Product> listByDataGrid(Integer aid,DataGrid<Product> dg, Form f)
    {
        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        f.addFrom(f,"status-i",1+"");

        return super.listByDataGrid("select yzwstock_product.*,yzwstock_product_category.name as category_name from yzwstock_product left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id", dg, f);
    }


    public List<Product> listbyProduct(Integer aid){
        StringBuffer sql = new StringBuffer();
        sql.append("select yzwstock_product.*,yzwstock_product_category.name as category_name from yzwstock_product left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id ");
        if(aid != null) {
            sql.append(" where yzwstock_product.aid='"+aid+"'");
        }
        List<Product> StockProductReplenish = find(sql.toString());
        for(Product product:StockProductReplenish){
            String  productName = product.get("name").toString() + "("+product.get("spec")+")";
            product.put("productName",productName);
        }
        return StockProductReplenish;
    }


    public DataGrid<Product> productStockQueryByGrid(Integer aid,DataGrid<Product> dg, Form f)
    {

        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        //f.addFrom(f,"status-i",1+"");

        String whereSql = " from  yzwstock_product left join  (select product_id,count(*) as stock_nums from yzwstock_stock_product where status = 1  group by product_id) sp on yzwstock_product.id = sp.product_id left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id";
        return listByDataGrid("select yzwstock_product.*,IFNULL(sp.stock_nums,0) as stock_nums,yzwstock_product_category.name as category_name ", whereSql, dg, f);
    }

    public Product echartsProducts(){
        return  Product.dao.findFirst("select yzwstock_product.*,IFNULL(sp.stock_nums,0) as stock_nums,yzwstock_product_category.name as category_name from  yzwstock_product left join  (select product_id,count(*) as stock_nums from yzwstock_stock_product where status = 1  group by product_id) sp on " +
                "yzwstock_product.id = sp.product_id left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id  GROUP BY name");
    }

    public DataGrid<Product> productStockQueryByGrid(Integer aid,Integer userid,DataGrid<Product> dg, Form f)
    {

        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        f.addFrom(f,"status-i",1+"");

        String whereSql = " from  yzwstock_product left join  (select product_id,yzwstock_stock.id as stockid,count(*) as stock_nums from yzwstock_stock_product left join yzwstock_stock on yzwstock_stock.id=yzwstock_stock_product.stock_id  where yzwstock_stock_product.status = 1 and yzwstock_stock.admin = "+userid+" group by product_id) sp on yzwstock_product.id = sp.product_id left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id";
        return listByDataGrid("select yzwstock_product.*,IFNULL(sp.stock_nums,0) as stock_nums,yzwstock_product_category.name as category_name,sp.stockid  ", whereSql, dg, f);
    }

    public List<Tree> getTree(Integer aid,Integer passId)
    {
        // 根据用户角色来获取 列表
        List<Tree> trees = new ArrayList<Tree>();

        for (Product cat : dao.list("where aid=? ",aid))
        {
            if(cat.getId().equals(passId)) continue;
            Tree tree = new Tree(cat.getId(), cat.getPid(), cat.getName(), "box", cat, false);
            trees.add(tree);
        }

        return trees;
    }

    public DataGrid<Product> stockDetailQueryByGrid(Integer aid,Integer productid,Integer stockid,String code,DataGrid<Product> dg, Form f)
    {

        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        f.addFrom(f,"status-i",1+"");
        f.addFrom(f,"id-i",productid+"");
        f.addFrom(f,"yzwstock_stock_product.stock_id-i",stockid+"");
        f.addFrom(f,"yzwstock_stock_product.status-i",1+"");
        if(StringUtils.isNotBlank(code)){
            f.addFrom(f,"yzwstock_in_storage.code-i",code);
        }

        String whereSql = "from yzwstock_stock_product left join yzwstock_product on yzwstock_product.id = yzwstock_stock_product.product_id left join yzwstock_product_category on yzwstock_product.category_id=yzwstock_product_category.id left join yzwstock_in_storage on yzwstock_in_storage.id= yzwstock_stock_product.instorage_id left join yzwstock_stock on yzwstock_stock.id = yzwstock_stock_product.stock_id";
        return listByDataGrid("select yzwstock_product.id as product_id, yzwstock_product.name,yzwstock_stock_product.*,yzwstock_product_category.name as category_name,yzwstock_stock.name as stockName,yzwstock_stock.id as stockid,yzwstock_in_storage.code as incode,yzwstock_in_storage.supplier_id ", whereSql, dg, f);
    }

    public DataGrid<Product> getstockListsql(Integer aid,Integer user_id,Integer productid,DataGrid<Product> dg, Form f){
//        f.addFrom(f, "stockdata.product_id-i",productid+"");
//        f.addFrom(f, "stockdata.aid-i", aid + "");
//
//        String whereSql = "";
//        if(user_id==null){
//            whereSql = "from (select spr.product_id, spr.aid,spr.status,st.admin,st.name as stockName, st.id as stockid, pr.name as productName, count(stock_id) as stocksum,spr.stock_id from yzwstock_stock_product spr left join yzwstock_stock st on st.id = spr.stock_id left join yzwstock_product pr on pr.id = spr.product_id GROUP BY spr.stock_id,product_id) as stockdata";
//        }else{
//            f.addFrom(f, "stockdata.admin-i", user_id+"");
//            whereSql = "from (select spr.product_id, spr.aid,spr.status,st.admin,st.name as stockName, st.id as stockid, pr.name as productName, count(stock_id) as stocksum,spr.stock_id from yzwstock_stock_product spr left join yzwstock_stock st on st.id = spr.stock_id left join yzwstock_product pr on pr.id = spr.product_id GROUP BY spr.stock_id,product_id) as stockdata";
//        }
//        f.addFrom(f, "stockdata.status-i", 1 + "");
//        String headSql = "select * ";
//        return listByDataGrid(headSql, whereSql, dg, f);

        f.addFrom(f, "spr.product_id-i",productid+"");
        f.addFrom(f, "spr.aid-i", aid + "");
        f.addFrom(f, "spr.status-i", 1 + " GROUP BY spr.stock_id");
        if(user_id != null){
            f.addFrom(f, "st.admin-i", user_id+"");
        }
        String whereSql = "from yzwstock_stock_product spr left join yzwstock_stock st on st.id = spr.stock_id left join yzwstock_product pr on pr.id = spr.product_id";
        String headSql = "select st.name as stockName, st.id as stockid, pr.name as productName, count(stock_id) as stocksum";
        return listByDataGrid(headSql, whereSql, dg, f);
    }

    public Product getProductByWxGoodsCodeAndGoodsSpec(String goodsCode,String goodsSpec){
        return  Product.dao.findFirst("select * from " + tableName + " where wx_goods_code = ? and specCode = ?", goodsCode, goodsSpec);
    }



}
