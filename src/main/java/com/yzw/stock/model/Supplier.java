package com.yzw.stock.model;

import com.jfinal.ext.kit.ModelExt;
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
@TableBind(tableName = "yzwstock_supplier")
public class Supplier extends EasyuiModel<Supplier> {
    public static Supplier dao = new Supplier();
    public DataGrid<Supplier> listByDataGrid(Integer aid,DataGrid<Supplier> dg, Form f)
    {
        f.addFrom(f, "aid-i", aid + "");//添加  aid 获取数据
        if(StringUtils.isBlank(f.getFromParm("status"))){
            f.integerValue.remove("status");
            f.addFrom(f,"status-in","1,2");
        }
        return super.listByDataGrid("select yzwstock_supplier.* from yzwstock_supplier ", dg, f);
    }

    public List<Tree> getTree(Integer aid,Integer passId)
    {
        // 根据用户角色来获取 列表
        List<Tree> trees = new ArrayList<Tree>();

        for (Supplier cat : dao.list("where aid=? ",aid))
        {
            if(cat.getId().equals(passId)) continue;
            Tree tree = new Tree(cat.getId(), cat.getPid(), cat.getName(), "box", cat, false);
            trees.add(tree);
        }

        return trees;
    }

}
