package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_stock_product")
public class StockProduct extends EasyuiModel<StockProduct> {
    public static StockProduct dao = new StockProduct();



    public StockProduct getByCode(String code){
        return findFirst("select * from "+tableName+" where code = ?",code);
    }

    public DataGrid<StockProduct> queryOutProductDetailGrid(DataGrid<StockProduct> dg,Form f){
        return this.listByDataGrid("select * ", "from yzwstock_stock_product ", dg, f);
    }

    public DataGrid<StockProduct> listByStockProductDataGrid(Integer inId,String code,String product_name,DataGrid<StockProduct> dg, Form f){
        String sql = "select yzwstock_stock_product.*,yzwstock_product.name,yzwstock_product.spec,yzwstock_stock.name as stock_name from yzwstock_stock_product left join yzwstock_product on yzwstock_stock_product.product_id = yzwstock_product.id left join yzwstock_stock on yzwstock_stock_product.stock_id = yzwstock_stock.id";

        if(!StringUtils.isBlank(product_name)){
            f.addFrom(f, "yzwstock_product.name-*", product_name);
        }
        f.addFrom(f, "yzwstock_stock_product.instorage_id-i", inId + "");
        f.addFrom(f, "yzwstock_stock_product.code-*", code);
        //f.addFrom(f, "yzwstock_stock_product.status-i", "1");
        return listByDataGrid(sql, dg, f);
    }

    public DataGrid<StockProduct> listByStockProductDataGrid(Integer user_id,Integer inId,String code,String product_name,DataGrid<StockProduct> dg, Form f){
        if(!StringUtils.isBlank(product_name)){
            f.addFrom(f, "yzwstock_product.name-*", product_name);
        }
        if(user_id!=null){
            f.addFrom(f,"yzwstock_stock.admin-i",user_id+"");
        }
        String sql = "select yzwstock_stock_product.*,yzwstock_product.name,yzwstock_product.spec,yzwstock_stock.name as stock_name from yzwstock_stock_product left join yzwstock_product on yzwstock_stock_product.product_id = yzwstock_product.id left join yzwstock_stock on yzwstock_stock_product.stock_id = yzwstock_stock.id  and yzwstock_stock.admin="+user_id+" ";
        f.addFrom(f, "yzwstock_stock_product.instorage_id-i", inId + "");
        f.addFrom(f, "yzwstock_stock_product.code-*", code);
        //f.addFrom(f, "yzwstock_stock_product.status-i", "1");

        return listByDataGrid(sql, dg, f);
    }

//    public List<StockProduct> listByReplenish(Integer aId, Integer status){
//        List<StockProduct> StockProductReplenish =find("select * from(select st.name as stockName,pr.name as productName,pr.spec as spec,sp.stock_id,product_id,count(product_id) as stockSum,sw.warning_first,sw.warning_secend,sw.worning_third \n" +
//                " from yzwstock_stock_product sp left join yzwstock_stock st on sp.stock_id = st.id " +
//                " left join yzwstock_product pr on pr.id = sp.product_id left join yzwstock_product_stock_worning sw on sw.stockid = sp.stock_id " +
//                " where sp.aid = ? and sp.status = ? GROUP BY product_id,stock_id order by stock_id)t1 where stockSum < warning_first or stockSum < warning_secend or stockSum < worning_third\n", aId, status);
//        return StockProductReplenish;
//    }


    public List<StockProduct> listByReplenish(Integer userId,Integer aId, Integer status,String warnning_category_name,String warnning_stock_name) {
            StringBuffer sql = new StringBuffer("SELECT t1.* ,sw.warning_first,sw.warning_secend,sw.worning_third FROM (SELECT st. NAME AS stockName,pr. NAME AS productName,pr.spec AS spec,sp.stock_id,sp.product_id,count(sp.product_id) AS stockSum ");
                    sql.append(" FROM yzwstock_stock_product sp ");
                    sql.append(" LEFT JOIN yzwstock_stock st ON sp.stock_id = st.id ");
                    sql.append(" LEFT JOIN yzwstock_product pr ON pr.id = sp.product_id ");
                    sql.append(" LEFT JOIN yzwstock_product_stock_worning sw ON sw.stockid = sp.stock_id");
                    sql.append(" where sp.status = ?");
             if(aId != null){
                    sql.append(" and sp.aid ="+aId);
             }
            if(userId != null){
                    sql.append(" and st.admin='"+userId+"'");
            }
            sql.append(" GROUP BY product_id,stock_id order by stock_id)t1 LEFT JOIN yzwstock_product_stock_worning sw on t1.product_id=sw.productid where 1=1");
            if(StringUtils.isNotBlank(warnning_category_name)) {
                    sql.append(" and stockName like '%"+warnning_category_name+"%' ");
            }
            if(StringUtils.isNotBlank(warnning_stock_name)) {
                sql.append(" and productName like '%"+warnning_stock_name+"%'");
            }
            sql.append(" and (stockSum < warning_first or stockSum < warning_secend or stockSum < worning_third)");
/*            List<StockProduct> StockProductReplenish = find("select * from(select st.name as stockName,pr.name as productName,sp.stock_id,product_id,count(product_id) as stockSum,sw.warning_first,sw.warning_secend,sw.worning_third \n" +
                    " from yzwstock_stock_product sp left join yzwstock_stock st on sp.stock_id = st.id " +
                    " left join yzwstock_product pr on pr.id = sp.product_id left join yzwstock_product_stock_worning sw on sw.stockid = sp.stock_id " +
                    " where sp.aid = ? and sp.status = ? GROUP BY product_id,stock_id order by stock_id)t1 where  and productName like '%"+warnning_stock_name+"%' and (stockSum < warning_first or stockSum < warning_secend or stockSum < worning_third)\n", aId, status);
            */

        List<StockProduct> StockProductReplenish = find(sql.toString(), status);
        return StockProductReplenish;
    }



    public static boolean hasFullOutNums(int stock_id,int product_id,Integer nums){
        if(getProdctStockNumsWithExcludelocking(stock_id, product_id)>=nums){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 获取库存中某货物的库存数（包含锁定的数）
     * @param stock_id
     * @param product_id
     * @return
     */
    public static Long getProdctStockNums(int stock_id,int product_id){
        long nums = 0L;
        if(stock_id == 0){
             nums = StockProduct.dao.getCount("select * from yzwstock_stock_product where  product_id = ? and outstorage_id is null and status = 1",product_id);
        }else{
            nums = StockProduct.dao.getCount("select * from yzwstock_stock_product where stock_id = ? and product_id = ? and outstorage_id is null and status = 1",stock_id,product_id);
        }
        return nums;
    }

    /**
     * 获取库存中某货物的库存数（不包含锁定的数）
     * @param stock_id
     * @param product_id
     * @return
     */
    public static Long getProdctStockNumsWithExcludelocking(int stock_id,int product_id){
        long nums = getProdctStockNums(stock_id,product_id);
        //System.out.println("仓库中总的货物数："+nums);
        //正在出库且未完成的所有出库单中该货物的将要出库总数（包括未出库和已经出库的）
        OutStorageDetail outStorageDetail = null;
        if(stock_id == 0) {
            outStorageDetail = OutStorageDetail.dao.findFirst("select IFNULL(sum(product_num),0) as willOutStockNums from yzwstock_out_storage_detail where  product_id = ?",product_id);
        }else {
             outStorageDetail = OutStorageDetail.dao.findFirst("select IFNULL(sum(product_num),0) as willOutStockNums from yzwstock_out_storage_detail where stock_id = ? and product_id = ?", stock_id, product_id);
        }
        long willOutStockNums = 0L;
        if(outStorageDetail!=null){
            willOutStockNums = outStorageDetail.getBigDecimal("willOutStockNums").longValue();
        }
        //正在出库且未完成的所有出库单中该货物已经出库的总数（已经出库的）
        long endOutStockSumNums = 0L;

        List<OutStorageDetail> outStorageDetailList = null;
        if(stock_id == 0){
            outStorageDetailList = OutStorageDetail.dao.find("select *  from yzwstock_out_storage_detail where product_id = ?",product_id);
        }else{
            outStorageDetailList = OutStorageDetail.dao.find("select *  from yzwstock_out_storage_detail where stock_id = ? and product_id = ?",stock_id,product_id);
        }
        Long endOutStockNums = 0L;
        for(OutStorageDetail outStorageDetail1:outStorageDetailList){
            //int product_nums = outStorageDetail.getInt("product_num");
            if(stock_id == 0){
                endOutStockNums = StockProduct.dao.getCount("select * from yzwstock_stock_product where product_id = ? and outstorage_id = ?  and (status = 3 or status = 2)",product_id, outStorageDetail1.get("outstorage_id"));
            }else{
                endOutStockNums = StockProduct.dao.getCount("select * from yzwstock_stock_product where stock_id = ? and product_id = ? and outstorage_id = ?  and (status = 3 or status = 2)", stock_id,product_id, outStorageDetail1.get("outstorage_id"));

            }
            endOutStockSumNums +=endOutStockNums;
        }
        long lookingNumsWillOutStockNums = willOutStockNums-endOutStockSumNums;
        //System.out.println("仓库中已经被锁定将要出库的货物数："+lookingNumsWillOutStockNums);
        return nums-lookingNumsWillOutStockNums;
    }

    public DataGrid<StockProduct> queryStockData(DataGrid<StockProduct> dg,Integer aid,Integer stockId,Form f,String search_product)
    {
        String sqlSelect = "select * ";
        String sql=null;
        if(search_product!=null){
             sql = " from (SELECT stock_id as stockid,product_id,count(*) as nums,c.name as product_name, c.spec as product_spec FROM (select * from yzwstock_stock_product where stock_id = "+stockId+" and status = 1 ) a left join yzwstock_stock b on a.stock_id=b.id left join yzwstock_product c on a.product_id = c.id where b.aid = "+aid+" and c.name like '%"+search_product+"%' GROUP BY stock_id,product_id) as stockdata";
        }else{
             sql = " from (SELECT stock_id as stockid,product_id,count(*) as nums,c.name as product_name, c.spec as product_spec FROM (select * from yzwstock_stock_product where stock_id = "+stockId+" and status = 1 ) a left join yzwstock_stock b on a.stock_id=b.id left join yzwstock_product c on a.product_id = c.id where b.aid = "+aid+"  GROUP BY stock_id,product_id) as stockdata";
        }

        DataGrid<StockProduct> dataGrid = listByDataGrid(sqlSelect,sql,dg,f);
        for(StockProduct stockProduct:dataGrid.getRows()){
            String  productName = stockProduct.get("product_name").toString() + "("+stockProduct.get("product_spec")+")";
            long enablenums =   this.getProdctStockNumsWithExcludelocking(stockId,stockProduct.getInt("product_id"));
            stockProduct.put("lockingnums",stockProduct.getLong("nums")-enablenums);
            stockProduct.put("enablenums",enablenums);
            stockProduct.put("productName",productName);
        }
        return dataGrid;
    }



}
