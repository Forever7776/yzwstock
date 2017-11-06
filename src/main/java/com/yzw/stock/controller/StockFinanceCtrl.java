package com.yzw.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.log.Logger;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.stock.model.InStorage;
import com.yzw.stock.model.Stock;
import com.yzw.stock.service.StockFinanceService;
import com.yzw.system.model.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xxh on 2015/9/18.
 */
@ControllerBind(controllerKey = "/stock/finance", viewPath = UrlConfig.STOCK)
public class StockFinanceCtrl extends EasyuiController<Stock> {
    private Logger logger = Logger.getLogger(this.getClass());
    StockFinanceService service = new StockFinanceService();
    public void index(){
        render("stock.finance.html");
    }

    public void queryDailyStockFinance(){
        String startDate = getPara("startDate");
        String endDate = getPara("endDate");
        String stock_name=getPara("stock_name");
        renderJson(Stock.dao.queryDailyStockFinance(startDate, endDate, getDataGrid(), getAid(), stock_name));
    }

    public void queryTotalStock(){
        renderJson(Stock.dao.queryTotalStockFinance(getAid()));
    }

    public void printStockFinanceChoose(){
        //获得选择时间数据
        String start=this.getPara("start");
        String end=this.getPara("end");
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account account=Account.dao.findById(getAid());
        List<Stock> stockIn=new ArrayList<Stock>();//入库统计
        List<Stock> stockOut=new ArrayList<Stock>();;//出库统计
        List<Stock> list=new ArrayList<Stock>();;//出库统计
        if(start.equals(end)){
            stockIn=Stock.dao.find("SELECT SUM(t.in_num) as nums,SUM(t.in_amount) as amount,t.stock_name,t.in_date from\n" +
                    "(select aa.stock_name,\n" +
                    "       aa.stock_id,\n" +
                    "       IFNULL(count(d.id),0) as in_num,\n" +
                    "       IFNULL(sum(aa.nums * aa.price),0) as in_amount,\n" +
                    "       DATE_FORMAT(d.insert_date,'%Y-%m-%d') as in_date\n" +
                    "from (select distinct\n" +
                    "             a.aid,\n" +
                    "             a.name as stock_name,\n" +
                    "             a.id as stock_id,\n" +
                    "             b.instorage_id,\n" +
                    "             b.nums,\n" +
                    "             b.price\n" +
                    "      from yzwstock_stock a\n" +
                    "      left join yzwstock_in_storage_detail b on a.id = b.stock_id) aa\n" +
                    "left join yzwstock_in_storage d on aa.instorage_id = d.id\n" +
                    "where 1=1 and d.status = 3 and aa.aid=?  and d.insert_date like '%"+start+"%' group by aa.stock_id,in_date order by in_date)t GROUP BY t.stock_name",getAid());

            stockOut=Stock.dao.find("SELECT SUM(t.out_num) as nums,SUM(t.out_amount) as amount,t.stock_name,t.out_date from\n" +
                    "(select aa.stock_name,\n" +
                    "       aa.stock_id,\n" +
                    "       IFNULL(count(e.id),0) as out_num,\n" +
                    "       IFNULL(sum(aa.product_num * aa.product_price),0) as out_amount,\n" +
                    "       DATE_FORMAT(e.insert_date,'%Y-%m-%d') as out_date\n" +
                    "from (select distinct\n" +
                    "             a.aid,\n" +
                    "             a.name as stock_name,\n" +
                    "             a.id as stock_id,\n" +
                    "             c.outstorage_id,\n" +
                    "             c.product_num,\n" +
                    "             c.product_price\n" +
                    "      from yzwstock_stock a\n" +
                    "      left join yzwstock_out_storage_detail c on a.id = c.stock_id) aa\n" +
                    "left join yzwstock_out_storage e on aa.outstorage_id = e.id\n" +
                    "where 1=1 and e.status = 6 and aa.aid=? and e.insert_date like '%"+start+"%' group by aa.stock_id,out_date order by out_date) t GROUP BY t.stock_name",getAid()    );
        }else{
            stockIn=Stock.dao.find("SELECT SUM(t.in_num) as nums_in,SUM(t.in_amount) as amount_in,t.stock_name,t.in_date from\n" +
                    "(select aa.stock_name,\n" +
                    "       aa.stock_id,\n" +
                    "       IFNULL(count(d.id),0) as in_num,\n" +
                    "       IFNULL(sum(aa.nums * aa.price),0) as in_amount,\n" +
                    "       DATE_FORMAT(d.insert_date,'%Y-%m-%d') as in_date\n" +
                    "from (select distinct\n" +
                    "             a.aid,\n" +
                    "             a.name as stock_name,\n" +
                    "             a.id as stock_id,\n" +
                    "             b.instorage_id,\n" +
                    "             b.nums,\n" +
                    "             b.price\n" +
                    "      from yzwstock_stock a\n" +
                    "      left join yzwstock_in_storage_detail b on a.id = b.stock_id) aa\n" +
                    "left join yzwstock_in_storage d on aa.instorage_id = d.id\n" +
                    "where 1=1 and d.status = 3 and aa.aid=?  and d.insert_date >=? and d.insert_date<=? group by aa.stock_id,in_date order by in_date)t GROUP BY t.stock_name\n" +
                    "\n",getAid(),start,end);

            stockOut=Stock.dao.find("SELECT SUM(t.out_num) as nums_out,SUM(t.out_amount) as amount_out,t.stock_name,t.out_date from\n" +
                    "(select aa.stock_name,\n" +
                    "       aa.stock_id,\n" +
                    "       IFNULL(count(e.id),0) as out_num,\n" +
                    "       IFNULL(sum(aa.product_num * aa.product_price),0) as out_amount,\n" +
                    "       DATE_FORMAT(e.insert_date,'%Y-%m-%d') as out_date\n" +
                    "from (select distinct\n" +
                    "             a.aid,\n" +
                    "             a.name as stock_name,\n" +
                    "             a.id as stock_id,\n" +
                    "             c.outstorage_id,\n" +
                    "             c.product_num,\n" +
                    "             c.product_price\n" +
                    "      from yzwstock_stock a\n" +
                    "      left join yzwstock_out_storage_detail c on a.id = c.stock_id) aa\n" +
                    "left join yzwstock_out_storage e on aa.outstorage_id = e.id\n" +
                    "where 1=1 and e.status = 6 and aa.aid=? and e.insert_date >=? and e.insert_date <=? group by aa.stock_id,out_date order by out_date) t GROUP BY t.stock_name",getAid(),start,end);
        }
        list.addAll(stockIn);
        list.addAll(stockOut);
        service.StockFinanceChooseTableTempateExport(1, path, list, account, this.getResponse(), null);
        this.renderNull();
    }



}
