package com.yzw.admin.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by admin on 2015/10/10.
 */
@TableBind(tableName = "admin_account")

public class AdminAccount extends EasyuiModel<AdminAccount> {
    private static final long serialVersionUID = -128801010211787215L;
    public static AdminAccount dao = new AdminAccount();


    public List<AdminAccount> list() {
        return find("select * from admin_account");
    }

    public AdminAccount edit(Integer userID) {
        UserManage user = UserManage.dao.findFirst("select * from system_user where id=?", userID);
        AdminAccount list = AdminAccount.dao.findFirst("select * from admin_account where id=?", user.getInt("aid"));
        return list;
    }



    public boolean save(String nickName, String pwd, String tel, String fax, String address) {
        if (StringUtils.isNotBlank(nickName) && StringUtils.isNotBlank(pwd) && StringUtils.isNotBlank(tel) && StringUtils.isNotBlank(fax) && StringUtils.isNotBlank(address)) {
            boolean list = AdminAccount.dao.set("nickname", nickName).set("pwd", pwd).set("tel", tel).set("fax", fax).set("address", address).save();
            return list;
        }
        return true;
    }
}
