package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;

import java.util.List;

/**
 * Created by admin on 2015/9/7.
 */
@TableBind(tableName = "yzwstock_product_stock_worning")
public class Warning extends EasyuiModel<Warning>{
    public static Warning dao = new Warning();
    public DataGrid<Warning> listByDetailDataGrid(Integer aid,Integer inId, DataGrid<Warning> dg, Form f){
        f.addFrom(f,"yzwstock_product.aid-i",aid+"");
        String whereSql = "from yzwstock_product left join (select * from yzwstock_product_stock_worning where stockid = "+inId+") as stockWorning on yzwstock_product.id=stockWorning.productid left join yzwstock_stock on yzwstock_stock.id =stockWorning.stockid ";
        String headSql = "select stockWorning.*,yzwstock_product.name as product_name,yzwstock_stock.name as stock_name,yzwstock_product.id as productid, yzwstock_product.spec as spec";
        return listByDataGrid(headSql, whereSql, dg, f);
    }

    public DataGrid<Warning> listByDetailDataGrid(Integer aid, Integer inId, String search, DataGrid<Warning> dg, Form f){
        f.addFrom(f,"proc.aid-i",aid+"");
        String whereSql = "from (select * from yzwstock_product where  yzwstock_product.name like '%"+search+"%') as proc left join (select * from yzwstock_product_stock_worning where stockid = "+inId+") as stockWorning on proc.id=stockWorning.productid left join yzwstock_stock on yzwstock_stock.id =stockWorning.stockid ";
        String headSql = "select stockWorning.*,proc.name as product_name,yzwstock_stock.name as stock_name,proc.id as productid";
        return listByDataGrid(headSql, whereSql, dg, f);
    }

    public List<Warning> listByStockId(Integer aid){
        StringBuffer sb = new StringBuffer();
        sb.append("select sw.* from yzwstock_product_stock_worning sw left join yzwstock_stock a on a.id = sw.stockid where");
        if(aid != null){
            sb.append(" a.aid ="+aid+"");
        }
        List<Warning> list = find(sb.toString());
        return list;
    }
}
