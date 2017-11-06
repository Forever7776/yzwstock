package com.yzw.stock.model;

/**
 * Created by luomhy on 2015/7/23.
 */

import com.jfinal.ext.kit.ModelExt;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;


@TableBind(tableName = "yzwstock_reportbad")
public class Reportbad extends EasyuiModel<Reportbad> {
    public static Reportbad dao = new Reportbad();

    public DataGrid<Reportbad> listByDataGrid(Integer aid,DataGrid dg,Form f){
        f.addFrom(f,"aid-i",aid+"");

        return listByDataGrid("select yzwstock_reportbad.*,yzwstock_out_storage.id as outid", "from yzwstock_reportbad left join yzwstock_out_storage on yzwstock_out_storage.id = yzwstock_reportbad.outstorage_id ", dg, f);
    }

    public DataGrid<Reportbad> listByDataGrid(Integer userid,Integer aid,DataGrid dg,Form f){
        f.addFrom(f,"",aid+"");
        f.addFrom(f, "insert_user_id-i", userid + "");//添加 userid 获取数据
        return listByDataGrid("select yzwstock_reportbad.*,yzwstock_out_storage.id as outid", "from yzwstock_reportbad left join yzwstock_out_storage on yzwstock_out_storage.id = yzwstock_reportbad.outstorage_id ", dg, f);
    }

    public DataGrid<Reportbad> detaByDataGrid(Integer aid,Integer userid, Integer id, DataGrid dg, Form f){
        f.addFrom(f,"yr.aid-i",aid+"");
        f.addFrom(f,"yr.id-i",id + "");
        if(userid != null){
            f.addFrom(f, "yr.insert_user_id-i", userid + "");//添加 userid 获取数据
        }
        String sql="select yr.code,yr.status,ysd.product_num,ys.name as stockName,yp.name as productName,yp.spec from yzwstock_reportbad yr" +
                   " left join yzwstock_out_storage_detail ysd on ysd.outstorage_id = yr.outstorage_id" +
                   " left join yzwstock_product yp on yp.id = ysd.product_id" +
                   " left join yzwstock_stock ys on ys.id = ysd.stock_id";
        return listByDataGrid(sql,dg,f);
    }

}
