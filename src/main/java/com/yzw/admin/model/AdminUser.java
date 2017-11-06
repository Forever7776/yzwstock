package com.yzw.admin.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.Model;

/**
 * Created by admin on 2015/10/14.
 */
@TableBind(tableName = "admin_user")
public class AdminUser extends Model<AdminUser>{
    private static final long serialVersionUID = -128801010211787215L;
    public static AdminUser dao = new AdminUser();


}
