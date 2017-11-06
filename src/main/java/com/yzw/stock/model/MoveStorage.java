package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;

import java.util.List;

/**
 * Created by ytc on 2015/8/25.
 * 调库表
 */

@TableBind(tableName = "yzwstock_movestore")
public class MoveStorage extends EasyuiModel<MoveStorage> {
    public static MoveStorage dao = new MoveStorage();
    public DataGrid<MoveStorage> listByDataGrid(Integer aid,DataGrid<MoveStorage> dg, Form f){
        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        String sql = "select "+tableName+".* from "+tableName+" left join yzwstock_in_storage  b on "+tableName+".instorage_id = b.id left JOIN yzwstock_out_storage c on "+tableName+".outstorage_id = c.id";
        return super.listByDataGrid(sql,dg,f);
    }

    public DataGrid<MoveStorage> listByDataGrid(Integer userid,Integer aid,DataGrid<MoveStorage> dg, Form f){
        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        f.addFrom(f, "insert_user_id-i", userid + "");//添加 userid 获取数据
        String sql = "select "+tableName+".* from "+tableName+" left join yzwstock_in_storage  b on "+tableName+".instorage_id = b.id left JOIN yzwstock_out_storage c on "+tableName+".outstorage_id = c.id";
        return super.listByDataGrid(sql,dg,f);
    }

    public DataGrid<MoveStorage> detailJsonDataGrid(Integer aid,Integer id,DataGrid<MoveStorage> dg, Form f){
        f.addFrom(f, "yw.aid-i", aid + "");//添加  aid 获取数据
        f.addFrom(f, "yw.id-i", id + "");//添加 userid 获取数据
        String sql = " select yw.move_code,ys.name as stock_in,ys1.name as stock_out,yp.name,yp.spec, ysd.nums,ysd.price from yzwstock_movestore yw " +
                     " left join yzwstock_in_storage_detail ysd on ysd.instorage_id = yw.instorage_id" +
                     " left join yzwstock_out_storage_detail yod on yod.outstorage_id = yw.outstorage_id" +
                     " left join yzwstock_product yp on yp.id = ysd.product_id" +
                     " left join yzwstock_stock ys on ys.id = ysd.stock_id" +
                     " left join yzwstock_stock ys1 on ys1.id = yod.stock_id";
        return super.listByDataGrid(sql,dg,f);
    }
}
