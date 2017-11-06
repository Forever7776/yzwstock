package com.yzw.commons.controller;

import java.io.File;
import java.security.interfaces.RSAPublicKey;

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
@ControllerBind(controllerKey = "/",viewPath= UrlConfig.INDEX)
public class IndexController extends Controller
{


	public void jump()
	{
		Log.dao.insert(this, Log.EVENT_VISIT);
		render(UrlConfig.VIEW_COMMON_JUMP);
	}

	public void initDb()
	{
		new InitService().initDb(getRequest().getRealPath("/static/file") + File.separator + "init.sql");
		forwardAction("/jump");
	}

	public void loginView(){
//		if (firstInto()) return;

		RSAPublicKey publicKey = RSA.getDefaultPublicKey();
		String modulus = new String(Hex.encodeHex(publicKey.getModulus().toByteArray()));
		String exponent = new String(Hex.encodeHex(publicKey.getPublicExponent().toByteArray()));

		setAttr("modulus", modulus);
		setAttr("exponent", exponent);
		render(UrlConfig.VIEW_COMMON_LOGIN);

	}

	private boolean firstInto()
	{
		String init = getCookie("init");
		if (init == null) setCookie("init", "init", 1000 * 60 * 60 * 24 * 365);
		render(UrlConfig.VIEW_COMMON_INIT);

		return StringUtils.isEmpty(init);
	}

	public void loginOut()
	{
		try
		{
			Subject subject = SecurityUtils.getSubject();
			subject.logout();

			renderTop(UrlConfig.LOGIN);

		} catch (AuthenticationException e)
		{
			e.printStackTrace();
			renderText("异常：" + e.getMessage());
		}
	}

	@Before(LoginValidator.class)
	public void login()
	{
		String[] result = RSA.decryptUsernameAndPwd(getPara("key"));

		try
		{
			UsernamePasswordToken token = new UsernamePasswordToken(result[0], DigestUtils.md5Hex(result[1]));
			Subject subject = SecurityUtils.getSubject();
			if (!subject.isAuthenticated())
			{
				token.setRememberMe(true);
				subject.login(token);
				subject.getSession(true).setAttribute(Consts.SESSION_USER, User.dao.findByName(result[0]));

			}

			Log.dao.insert(this, Log.EVENT_LOGIN);

			redirect("/");

		} catch (UnknownAccountException e)
		{

			forwardAction("用户名不存在", UrlConfig.LOGIN);

		} catch (IncorrectCredentialsException e)
		{
			forwardAction("密码错误", UrlConfig.LOGIN);

		} catch (LockedAccountException e)
		{
			forwardAction("对不起 帐号被封了", UrlConfig.LOGIN);
			e.printStackTrace();
		} catch (ExcessiveAttemptsException e)
		{
			forwardAction("尝试次数过多 请明天再试", UrlConfig.LOGIN);
		} catch (AuthenticationException e)
		{
			forwardAction("对不起 没有权限 访问", UrlConfig.LOGIN);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			forwardAction("请重新登录", UrlConfig.LOGIN);
		}

	}

	public void remoteLogin()
	{
		String username = this.getPara("u");
		String pwd = this.getPara("p");
		if(StringUtils.isBlank(username)||StringUtils.isBlank("pwd")){

			forwardAction("请重新登录", UrlConfig.LOGIN);
		}
		try
		{
			UsernamePasswordToken token = new UsernamePasswordToken(username,pwd);
			Subject subject = SecurityUtils.getSubject();
			if (!subject.isAuthenticated())
			{
				token.setRememberMe(true);
				subject.login(token);
				subject.getSession(true).setAttribute(Consts.SESSION_USER, User.dao.findByName(username));

			}

			Log.dao.insert(this, Log.EVENT_LOGIN);

			redirect("/");

		} catch (UnknownAccountException e)
		{

			forwardAction("用户名不存在", UrlConfig.LOGIN);

		} catch (IncorrectCredentialsException e)
		{
			forwardAction("密码错误", UrlConfig.LOGIN);

		} catch (LockedAccountException e)
		{
			forwardAction("对不起 帐号被封了", UrlConfig.LOGIN);
			e.printStackTrace();
		} catch (ExcessiveAttemptsException e)
		{
			forwardAction("尝试次数过多 请明天再试", UrlConfig.LOGIN);
		} catch (AuthenticationException e)
		{
			forwardAction("对不起 没有权限 访问", UrlConfig.LOGIN);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			forwardAction("请重新登录", UrlConfig.LOGIN);
		}

	}

	public void unauthorized()
	{

		render(UrlConfig.VIEW_ERROR_401);
	}

}
