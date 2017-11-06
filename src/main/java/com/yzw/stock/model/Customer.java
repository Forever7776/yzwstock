package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import com.yzw.base.model.easyui.Tree;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_customer")
public class Customer extends EasyuiModel<Customer> {
    public static Customer dao = new Customer();

    public DataGrid<Customer> listByDataGrid(Integer aid,DataGrid<Customer> dg, Form f)
    {
        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        if(StringUtils.isBlank(f.getFromParm("status"))){
            f.integerValue.remove("status");
            f.addFrom(f,"status-in","1,2");
        }
        return super.listByDataGrid("select yzwstock_customer.* from yzwstock_customer ", dg, f);
    }

    public List<Tree> getTree(Integer aid,Integer passId)
    {
        List<Tree> trees = new ArrayList<Tree>();
        for (Customer cat : dao.list("where aid=? ",aid))
        {
            if(cat.getId().equals(passId)) continue;
            Tree tree = new Tree(cat.getId(), cat.getPid(), cat.getName(), "status_online", cat, false);
            trees.add(tree);
        }

        return trees;
    }

}
