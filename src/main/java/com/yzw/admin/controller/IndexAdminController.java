package com.yzw.admin.controller;

import java.io.File;
import java.security.interfaces.RSAPublicKey;

import com.alibaba.fastjson.JSONObject;
import com.yzw.admin.model.AdminUser;
import com.yzw.admin.validator.AdminLoginValidator;
import com.yzw.base.config.Consts;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.commons.service.InitService;
import com.yzw.base.util.RSA;
import com.yzw.base.util.Sec;
import com.yzw.commons.validator.LoginValidator;
import com.yzw.system.model.Log;
import com.yzw.system.model.User;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;


import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;

/***
 *
 * 月落斜阳 灯火阑珊
 *
 *
 */
@ControllerBind(controllerKey = "/admin",viewPath= AdminUrlConfig.COMMON)
public class IndexAdminController extends Controller
{

    public void index(){
        render("login.html");
    }


    public void loginpost(){
        JSONObject data = new JSONObject();
        String username = getPara("username");
        String password = getPara("password");
        AdminUser name = AdminUser.dao.findFirst("select * from admin_user where name=?", username);
        AdminUser pwd = AdminUser.dao.findFirst("select * from admin_user where pwd=?",password);
        if(name ==null || pwd == null){
            data.put("result",false);
            data.put("msg","用户名或密码错误！");
            renderJson(data);
        }else{
            data.put("result",true);
            renderJson(data);
        }
    }


    public void login()
    {
        render(AdminUrlConfig.INDEX);
    }



}
