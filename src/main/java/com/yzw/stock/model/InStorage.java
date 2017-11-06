package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;

import java.util.List;


/**
 * Created by luomhy on 2015/7/23.
 * 入库单
 */
@TableBind(tableName = "yzwstock_in_storage")
public class InStorage extends EasyuiModel<InStorage> {
    public static InStorage dao = new InStorage();
    public DataGrid<InStorage> listByDataGrid(Integer aid,DataGrid<InStorage> dg, Form f){
        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        String sql = "select "+tableName+".*,yzwstock_supplier.name as supplier_name from "+tableName+" left join yzwstock_supplier on yzwstock_supplier.id ="+tableName+".supplier_id";
        return super.listByDataGrid(sql,dg, f);
    }

    /**
     * 分仓管理员获取数据
     * @param userid
     * @param aid
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<InStorage> listByDataGrid(Integer userid,Integer aid,DataGrid<InStorage> dg, Form f){
       /* f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        f.addFrom(f, "insert_user_id-i", userid + "");//添加 userid 获取数据*/

        String sqlSelect = "SELECT * ";
        String sqlWhere="  FROM(SELECT a.*,yzwstock_supplier.name as supplier_name  FROM yzwstock_in_storage a left join yzwstock_in_storage_detail b ON  a.id=b.instorage_id   " +
                "LEFT JOIN yzwstock_stock d on d.id=b.stock_id left join yzwstock_supplier on yzwstock_supplier.id = a.supplier_id where d.admin="+userid+" GROUP BY a.id, b.stock_id) t1";
       // String sql = "select "+tableName+".*,yzwstock_supplier.name as supplier_name from "+tableName+" left join yzwstock_supplier on yzwstock_supplier.id ="+tableName+".supplier_id";
        return super.listByDataGrid(sqlSelect,sqlWhere,dg, f);
    }


    public InStorage getInStorageData(Integer inid){
        InStorage inStorage = findById(inid);
        if(inStorage!=null){
            List<InStorageDetail> inStorageDetailList = InStorageDetail.dao.printListByDetail(inid);
            inStorage.put("detailList",inStorageDetailList);
            return inStorage;
        }
        return null;
    }
}
