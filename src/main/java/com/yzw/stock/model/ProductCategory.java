package com.yzw.stock.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_product_category")
public class ProductCategory extends EasyuiModel<ProductCategory> {
    public static ProductCategory dao = new ProductCategory();

    public List<Tree> getTree(Integer aid,Integer pid,Integer passId)
    {
        // 根据用户角色来获取 列表
        List<Tree> trees = new ArrayList<Tree>();

        for (ProductCategory cat : getChild(aid,pid))
        {
            if(cat.getId().equals(passId)) continue;
            Tree tree = new Tree(cat.getId(), cat.getPid(), cat.getName(), "box",  cat, false);
            tree.children = getTree(aid,cat.getId(),passId);
            if (tree.children.size() > 0) tree.changeState();
            trees.add(tree);
        }

        return trees;
    }
    public List<ProductCategory> getChild(Integer aid,Integer id){
        List<ProductCategory> list;
        if(id==null) list = dao.list("where aid=? and pid is null",aid);
        else list = dao.list("where aid=? and pid=?",aid,id);
        return list;
    }

    public ProductCategory getCategoryByWxCategoryCode(Object categoryCode){
        return ProductCategory.dao.findFirst("select * from " +tableName+ " where wx_category_code = ?",categoryCode);
    }
}
