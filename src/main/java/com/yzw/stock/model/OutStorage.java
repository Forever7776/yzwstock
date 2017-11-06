package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;

import java.util.List;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_out_storage")
public class OutStorage extends EasyuiModel<OutStorage> {
    public static OutStorage dao = new OutStorage();
    public DataGrid<OutStorage> listByDataGrid(Integer aid,DataGrid<OutStorage> dg, Form f){
        String sqlSelect = "SELECT * ";
        String sqlWhere="  FROM(SELECT a.*,yzwstock_customer.name as customer_name  FROM yzwstock_out_storage a left join yzwstock_out_storage_detail b ON  a.id=b.outstorage_id   " +
                "LEFT JOIN yzwstock_stock d on d.id=b.stock_id left join yzwstock_customer on yzwstock_customer.id = a.customer_id where a.aid = "+aid+"  GROUP BY a.id) t1";
        return super.listByDataGrid(sqlSelect,sqlWhere,dg, f);
    }

    public DataGrid<OutStorage> listByDataGrid(Integer userid,Integer aid,DataGrid<OutStorage> dg, Form f){
        //f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        //f.addFrom(f, "insert_user_id-i", userid + "");//添加 userid 获取数据
        //String sql = "select "+tableName+".*,yzwstock_customer.name as customer_name from "+tableName+" left join yzwstock_customer on yzwstock_customer.id ="+tableName+".customer_id";
        String sqlSelect = "SELECT * ";
        String sqlWhere="  FROM(SELECT a.*,yzwstock_customer.name as customer_name  FROM yzwstock_out_storage a left join yzwstock_out_storage_detail b ON  a.id=b.outstorage_id   " +
                "LEFT JOIN yzwstock_stock d on d.id=b.stock_id left join yzwstock_customer on yzwstock_customer.id = a.customer_id where d.admin="+userid+" GROUP BY a.id) t1";
        return super.listByDataGrid(sqlSelect,sqlWhere,dg, f);
    }



    public OutStorage getOutStorageData(Integer outid){
        OutStorage outStorage = findById(outid);
        if(outStorage!=null){
            List<OutStorageDetail> outStorageDetailList = OutStorageDetail.dao.listByDetail(outid);
            outStorage.put("detailList",outStorageDetailList);
            return outStorage;
        }
        return null;
    }

}
