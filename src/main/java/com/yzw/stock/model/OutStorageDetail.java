package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by liuyj on 2015/8/5.
 */
@TableBind(tableName = "yzwstock_out_storage_detail")
public class OutStorageDetail extends EasyuiModel<OutStorageDetail> {
    public static OutStorageDetail dao = new OutStorageDetail();
    public DataGrid<OutStorageDetail> listByDetailDataGrid(Integer user_id,Integer outId, String product_name,DataGrid<OutStorageDetail> dg, Form f){
        f.addFrom(f, "yzwstock_out_storage_detail.outstorage_id-i", outId + "");
        if(!StringUtils.isBlank(product_name)){
            f.addFrom(f,"yzwstock_product.name-*",product_name);
        }
        if(user_id != null){
            f.addFrom(f,"yzwstock_stock.admin-i",user_id+"");
        }
        String whereSql = "from yzwstock_out_storage_detail left join yzwstock_product on yzwstock_product.id=yzwstock_out_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id =yzwstock_out_storage_detail.stock_id ";
        String headSql = "select yzwstock_out_storage_detail.*,yzwstock_product.name as product_name,yzwstock_stock.name as stock_name ";
        return listByDataGrid(headSql,whereSql, dg, f);
    }
    public List<OutStorageDetail> listByDetail(Integer inId){
        List<OutStorageDetail> outStorageDetailList =find("select yzwstock_out_storage_detail.product_num,yzwstock_product.name,yzwstock_product.code,yzwstock_product.spec,yzwstock_out_storage_detail.product_price,yzwstock_product.unit,yzwstock_stock.name as stock_name from yzwstock_out_storage_detail left join yzwstock_product on yzwstock_product.id = yzwstock_out_storage_detail.product_id left join yzwstock_stock on yzwstock_stock.id = yzwstock_out_storage_detail.stock_id  where outstorage_id=?",inId);
        return outStorageDetailList;
    }

    public List<OutStorageDetail> listByAid(Integer aid){
        StringBuffer sb = new StringBuffer();
        sb.append("select dt.* from yzwstock_out_storage_detail dt left join yzwstock_out_storage a on a.id = dt.outstorage_id where ");
        sb.append(" a.aid="+aid+"");
        List<OutStorageDetail> list = find(sb.toString());
        return list;
    }
}
