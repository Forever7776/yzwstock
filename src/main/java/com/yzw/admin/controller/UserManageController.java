package com.yzw.admin.controller;

import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.yzw.admin.model.AdminAccount;
import com.yzw.admin.model.UserManage;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by admin on 2015/10/10.
 */
@ControllerBind(controllerKey = "/admin/user",viewPath= AdminUrlConfig.BASE)
public class UserManageController extends Controller {
    public void index(){
        this.setAttr("list",UserManage.dao.list());
        render("user.html");
    }

    public void list(){
        renderJson(UserManage.dao.list());
    }

    public void edit(){
        keepPara();
        renderJson(AdminAccount.dao.edit(getParaToInt("userID")));
    }

    public void update(){
        Integer userID=getParaToInt("userID");
        String tel=getPara("tel");
        String fax=getPara("fax");
        String address=getPara("address");
        if(StringUtils.isNotBlank(tel)&&StringUtils.isNotBlank(fax)&&StringUtils.isNotBlank(address)){
            renderJson(Db.update("update admin_account set tel=?,fax=?,address=? where id=?", tel, fax, address, userID));
        }
        redirect("/admin/user");
    }

    public  void add(){
        String nickname=getPara("nickname");
        String pwd=getPara("pwd");
        String tel=getPara("tel");
        String fax=getPara("fax");
        String address=getPara("address");
        renderJson(AdminAccount.dao.save(nickname,pwd, tel, fax, address));
        redirect("/admin/user");
    }

}