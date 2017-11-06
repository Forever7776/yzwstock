package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Tree;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_stock")
public class Stock extends EasyuiModel<Stock> {
    public static Stock dao = new Stock();
    public List<Stock> list(Integer aid){
        return dao.find("select a.*,b.name as pname ,s.name as user_name from "+tableName+" a left join "+tableName+" b on a.pid=b.id left join system_user s on s.id=a.admin where a.aid=? and a.status = 1",aid);
    }

    public List<Stock> list(Integer aid,String storage_name){
        return dao.find("select a.*,b.name as pname from "+tableName+" a left join "+tableName+" b on a.pid=b.id where a.aid=? and a.status = 1 and a.name like '%"+storage_name+"%'",aid);
    }


    public List<Tree> getTree(Integer aid, Integer id, Integer passId )
    {
        // 根据用户角色来获取 列表
        List<Tree> trees = new ArrayList<Tree>();

        for (Stock stock : getChild(aid,id))
        {
            if (stock.getId().equals(passId)) continue;
            Tree tree = new Tree(stock.getId(), stock.getPid(), stock.getName(), stock.getIconCls(), stock, false);

            tree.children = getTree(aid,stock.getId(), passId);
            if (tree.children.size() > 0) tree.changeState();

            trees.add(tree);
        }
        return trees;
    }

    public List<Tree> getTree(Integer aid, Integer id, Integer passId, Integer admin)
    {
        // 根据用户角色来获取 列表
        List<Tree> trees = new ArrayList<Tree>();

        for (Stock stock : getChild(aid,id,admin))
        {
            if (stock.getId().equals(passId)) continue;
            Tree tree = new Tree(stock.getId(), stock.getPid(), stock.getName(), stock.getIconCls(), stock, false);

            tree.children = getTree(aid,stock.getId(), passId, admin);
            if (tree.children.size() > 0) tree.changeState();

            trees.add(tree);
        }
        return trees;
    }

    public List<Stock> getChild(Integer aid, Integer id,Integer admin)
    {
        if (id == null) return dao.list(" where aid=? and  pid is null and status = 1 and admin=? order by insert_date desc",aid,admin);
        return dao.list(" where aid=? and pid = ? and status = 1  order by insert_date desc", aid, id);

    }

    public List<Stock> getChild(Integer aid, Integer id)
    {
        if (id == null) return dao.list(" where aid=? and  pid is null and status = 1 order by insert_date desc",aid);
        return dao.list(" where aid=? and pid = ? and status = 1  order by insert_date desc", aid, id);

    }
    public Stock getTopParent(Integer id){
        if(id!=null){
            Stock stock = getParent(id);
            return stock;
        }else{
            return null;
        }
    }

    public String getChildStockIds(Integer aid,Integer id,Integer admin){
        List<Stock> list = getChild(aid, id, admin);
        Iterator iterator =list.iterator();
        StringBuffer sonStockIds = new StringBuffer();
        while (iterator.hasNext()){
            sonStockIds.append(iterator.next().toString());
            if(iterator.hasNext()){
                sonStockIds.append(",");
            }
        }
        return sonStockIds.toString();
    }



    public Stock getParent(int id){
        Stock stock = findById(id);
        if(stock!=null&&stock.getInt("pid")!=null){
            return getParent(stock.getInt("pid"));
        }else{
            return stock;
        }
    }

    public DataGrid<Stock> queryDailyStockFinance(String startDate, String endDate, DataGrid<Stock> dg, Integer aid,String stock_name){
        DataGrid<Stock> dataGrid = new DataGrid<Stock>();
        //统计入库单
        // 名称
        String whereSql_1 = "where 1=1 and d.status = 3 and aa.aid="+aid+" ";
        String whereSql_2 = "where 1=1 and e.status = 6 and aa.aid="+aid+" ";
        if(stock_name!=null){
            whereSql_1+=" and stock_name like '%"+stock_name+"%'";
            whereSql_2+="and stock_name like '%"+stock_name+"%'";
        }
        String mainSql_1 = "select aa.stock_name,\n" +
                "       aa.stock_id,\n" +
                "       IFNULL(count(d.id),0) as in_num,\n" +
                "       IFNULL(sum(aa.nums * aa.price),0) as in_amount,\n" +
                "       DATE_FORMAT(d.insert_date,'%Y-%m-%d') as in_date\n" +
                "from (select distinct\n" +
                "             a.aid,\n"+
                "             a.name as stock_name,\n" +
                "             a.id as stock_id,\n" +
                "             b.instorage_id,\n" +
                "             b.nums,\n" +
                "             b.price\n" +
                "      from yzwstock_stock a\n" +
                "      left join yzwstock_in_storage_detail b on a.id = b.stock_id) aa\n" +
                "left join yzwstock_in_storage d on aa.instorage_id = d.id\n";

        String groupByAndSortSql_1 = " group by aa.stock_id,in_date order by in_date";

        //统计出库单(已完成出库)
        String mainSql_2 = "select aa.stock_name,\n" +
                "       aa.stock_id,\n" +
                "       IFNULL(count(e.id),0) as out_num,\n" +
                "       IFNULL(sum(aa.product_num * aa.product_price),0) as out_amount,\n" +
                "       DATE_FORMAT(e.insert_date,'%Y-%m-%d') as out_date\n" +
                "from (select distinct\n" +
                "             a.aid,\n"+
                "             a.name as stock_name,\n" +
                "             a.id as stock_id,\n" +
                "             c.outstorage_id,\n" +
                "             c.product_num,\n" +
                "             c.product_price\n" +
                "      from yzwstock_stock a\n" +
                "      left join yzwstock_out_storage_detail c on a.id = c.stock_id) aa\n" +
                "left join yzwstock_out_storage e on aa.outstorage_id = e.id\n";

        String groupByAndSortSql_2 = " group by aa.stock_id,out_date order by out_date";

        if(StringUtils.isNotBlank(startDate)){
            whereSql_1 += " and DATE_FORMAT(d.insert_date,'%Y-%m-%d') >= '"+ startDate +"'";
            whereSql_2 += " and DATE_FORMAT(e.insert_date,'%Y-%m-%d') >= '"+ startDate +"'";
        }

        if(StringUtils.isNotBlank(endDate)){
            whereSql_1 += " and DATE_FORMAT(d.insert_date,'%Y-%m-%d') <= '"+ endDate +"'";
            whereSql_2 += " and DATE_FORMAT(e.insert_date,'%Y-%m-%d') <= '"+ endDate +"'";
        }

        List<Stock> inList = find(mainSql_1 + whereSql_1 + groupByAndSortSql_1);
        List<Stock> outList = find(mainSql_2 + whereSql_2 + groupByAndSortSql_2);
        List<Stock> list = new ArrayList<Stock>();

        List<Stock> s1_List = new ArrayList<Stock>();
        List<Stock> s2_List = new ArrayList<Stock>();
        List<Stock> restList = new ArrayList<Stock>();

        //合并日期相同的入库单和出库单数据
        for(Stock s1 : inList){
            for(Stock s2 : outList){
                if(s1.getStr("in_date") != null && s2.getStr("out_date") != null){
                    if(s1.getStr("in_date").equals(s2.getStr("out_date")) && (s1.getInt("stock_id").compareTo(s2.getInt("stock_id"))) == 0){
                        Stock s= new Stock();

                        s.put("stock_id",s2.get("stock_id"));
                        s.put("stock_name",s2.get("stock_name"));
                        s.put("in_num",s1.get("in_num"));
                        s.put("out_num",s2.get("out_num"));
                        s.put("in_amount",s1.get("in_amount"));
                        s.put("out_amount", s2.get("out_amount"));
                        s.put("date", s2.get("out_date"));
                        list.add(s);

                        s1_List.add(s1);
                        s2_List.add(s2);
                    }
                }
            }
        }

        inList.removeAll(s1_List);
        outList.removeAll(s2_List);

        //只有入库,没有出库
        for(Stock s1 : inList){
            if(s1.get("in_date") != null){
                Stock s= new Stock();

                s.put("stock_id",s1.get("stock_id"));
                s.put("stock_name",s1.get("stock_name"));
                s.put("in_num",s1.get("in_num"));
                s.put("out_num",0);
                s.put("in_amount",s1.get("in_amount"));
                s.put("out_amount",0);
                s.put("date",s1.get("in_date"));
                list.add(s);
            }
        }

        //只有出库,没有入库
        for(Stock s2 : outList){
            if(s2.get("out_date") != null){
                Stock s = new Stock();

                s.put("stock_id",s2.get("stock_id"));
                s.put("stock_name",s2.get("stock_name"));
                s.put("in_num",0);
                s.put("out_num",s2.get("out_num"));
                s.put("in_amount",0);
                s.put("out_amount",s2.get("out_amount"));
                s.put("date",s2.get("out_date"));
                list.add(s);
            }
        }

        //按日期重新排序
        Collections.sort(list, new Comparator<Stock>() {
            @Override
            public int compare(Stock o1, Stock o2) {
                return o1.getStr("date").compareTo(o2.getStr("date"));
            }
        });

        if(CollectionUtils.isNotEmpty(list)){
            dataGrid.total = list.size();
        }else{
            dataGrid.total = 0;
        }

        int startIndex = (dg.page - 1) * dg.total;
        int endIndex = startIndex + dg.total;

        if(dataGrid.total < endIndex){
            dataGrid.rows = list.subList(startIndex,dataGrid.total);
        }else{
            dataGrid.rows = list.subList(startIndex,endIndex);
        }

//        String mainSql = "select a.name as stock_name,\n" +
//                         "       IFNULL(count(d.id),0) as in_num,IFNULL(count(e.id),0) as out_num,\n" +
//                         "       IFNULL(sum(d.total_fee),0) as in_amount,IFNULL(sum(e.total_fee),0) as out_amount,\n" +
//                         "       DATE_FORMAT(d.insert_date,'%Y-%m-%d') as in_date,\n" +
//                         "       DATE_FORMAT(e.insert_date,'%Y-%m-%d') as out_date\n" +
//                         "from yzwstock_stock a\n" +
//                         "left join yzwstock_in_storage_detail b on a.id = b.stock_id\n" +
//                         "left join yzwstock_out_storage_detail c on a.id = c.stock_id\n" +
//                         "left join yzwstock_in_storage d on b.instorage_id = d.id\n" +
//                         "left join yzwstock_out_storage e on c.outstorage_id = e.id\n";
//        String whereSql = "where 1=1";
//        String groupBySql = " group by a.id,in_date,out_date\n";
//        String limitAndSortSql = "order by in_date desc,out_date desc limit ?,?";
//
//        if(StringUtils.isNotBlank(startDate)){
//            whereSql += " and in_date >= '"+ startDate +"' or out_date >='"+ startDate +"'";
//        }
//
//        if(StringUtils.isNotBlank(endDate)){
//            whereSql += " and in_date <= '"+ endDate +"' or out_date <= '" + endDate + "'";
//        }
//
//        List<Stock> list = find(mainSql + whereSql + groupBySql);
//        if(CollectionUtils.isNotEmpty(list)){
//            dataGrid.total = list.size();
//        }else{
//            dataGrid.total = 0;
//        }
//
//        dataGrid.rows = find(mainSql + whereSql + groupBySql + limitAndSortSql,(dg.page - 1) * dg.total ,dg.total);

        return dataGrid;
    }

    public List<Stock> queryTotalStockFinance(Integer aid){
        //入库单总计
        List<Stock>  listInData=Stock.dao.find(
                "SELECT rs.rs.stock_name,rs.stock_id,sum(rs.in_num) in_num ,SUM(in_amount) in_amount FROM (select aa.stock_name,\n" +
                "                                aa.stock_id,\n" +
                "                                  IFNULL(count(d.id),0) as in_num,\n" +
                "                                 IFNULL(sum(aa.nums * aa.price),0) as in_amount,\n" +
                "                                  DATE_FORMAT(d.insert_date,'%Y-%m-%d') as in_date\n" +
                "                           from (select distinct\n" +
                "                                        a.aid,\n" +
                "                                        a.name as stock_name,\n" +
                "                                        a.id as stock_id,\n" +
                "                                        b.instorage_id,\n" +
                "                                        b.nums,\n" +
                "                                        b.price\n" +
                "                                 from yzwstock_stock a\n" +
                "                                 left join yzwstock_in_storage_detail b on a.id = b.stock_id) aa\n" +
                "                           left join yzwstock_in_storage d on aa.instorage_id = d.id\n" +
                "        where 1=1 and d.status = 3 and aa.aid=aa.aid="+aid+"\n "+
                "        group by aa.stock_id,in_date order by in_date ) rs GROUP BY rs.stock_name");
        return  listInData;
    }

}
