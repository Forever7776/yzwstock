package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;

import java.util.List;

/**
 * Created by luomhy on 2015/7/23.
 * 入库单详情
 */
@TableBind(tableName = "yzwstock_in_storage_detail")
public class InStorageDetail extends EasyuiModel<InStorageDetail> {
    public static InStorageDetail dao = new InStorageDetail();

    public DataGrid<InStorageDetail> listByDetailDataGrid(Integer inId,DataGrid<InStorageDetail> dg, Form f){
        String whereSql = "from yzwstock_in_storage_detail left join yzwstock_product on yzwstock_product.id=yzwstock_in_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id =yzwstock_in_storage_detail.stock_id ";
        String headSql = "select yzwstock_in_storage_detail.*,yzwstock_product.name as product_name,yzwstock_stock.name as stock_name ,yzwstock_product.spec as spec ";



        f.addFrom(f,"instorage_id-i",inId+"");
        return listByDataGrid(headSql,whereSql, dg, f);
    }

    public DataGrid<InStorageDetail> listByDetailDataGrid(Integer user_id,Integer inId,DataGrid<InStorageDetail> dg, Form f){
        String whereSql = "from yzwstock_in_storage_detail left join yzwstock_product on yzwstock_product.id=yzwstock_in_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id =yzwstock_in_storage_detail.stock_id ";
        String headSql = "select yzwstock_in_storage_detail.*,yzwstock_product.name as product_name,yzwstock_stock.name as stock_name ,yzwstock_product.spec as spec ";
        if(user_id!=null){
            f.addFrom(f,"yzwstock_stock.admin-i",user_id+"");
        }
        f.addFrom(f,"instorage_id-i",inId+"");

        return listByDataGrid(headSql, whereSql, dg, f);
    }


    public List<InStorageDetail> listByDetail(Integer inId){
        return find("select * from yzwstock_in_storage_detail where instorage_id=?", inId);
    }

    public List<InStorageDetail> printListByDetail(Integer inId){
        List<InStorageDetail> inStorageDetailList =find("select yzwstock_in_storage_detail.nums,yzwstock_product.name,yzwstock_product.code,yzwstock_product.spec,yzwstock_in_storage_detail.price,yzwstock_product.unit,yzwstock_stock.name as stock_name from yzwstock_in_storage_detail left join yzwstock_product on yzwstock_product.id = yzwstock_in_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id = yzwstock_in_storage_detail.stock_id  where instorage_id=?",inId);
        return inStorageDetailList;
    }

    public List<InStorageDetail> listByAid(Integer aid){
        StringBuffer sb = new StringBuffer();
        sb.append("select dt.* from yzwstock_in_storage_detail dt left join yzwstock_in_storage a on a.id = dt.instorage_id where ");
        sb.append(" a.aid="+aid+"");
        List<InStorageDetail> list = find(sb.toString());
        return list;
    }
}
