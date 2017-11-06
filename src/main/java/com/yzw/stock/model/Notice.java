package com.yzw.stock.model;

import com.jfinal.ext.kit.ModelExt;
import com.jfinal.ext.plugin.tablebind.TableBind;
//import com.sun.tools.corba.se.idl.constExpr.Not;
import com.yzw.base.jfinal.ext.model.EasyuiModel;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_notice")
public class Notice extends EasyuiModel<Notice> {
    public static final Notice dao = new Notice();
    public static void abc(){
        Notice notice = new Notice();
        notice.set("aod",1).set("b",2);

        notice.saveOrUpdate();
    }
}
