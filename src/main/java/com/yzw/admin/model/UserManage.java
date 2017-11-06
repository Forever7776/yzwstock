package com.yzw.admin.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.Model;

import java.util.List;

/**
 * Created by admin on 2015/10/10.
 */
@TableBind(tableName = "system_user")

public class UserManage extends Model<UserManage>{
    private static final long serialVersionUID = -128801010211787215L;

    public static UserManage dao = new UserManage();

    public List<UserManage> list(){
        return find("select * from system_user");
    }


}
