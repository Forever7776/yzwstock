package com.yzw.commons.controller;


import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.ehcache.CacheName;
import com.jfinal.plugin.ehcache.EvictInterceptor;
import com.yzw.base.config.Consts;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ShiroExt;
import com.yzw.base.jfinal.ext.ctrl.JsonController;
import com.yzw.commons.model.Msg;
import com.yzw.commons.validator.MsgValidator;
import com.yzw.system.model.User;

@CacheName(value = "/index/msg")
@ControllerBind(controllerKey = "/index/msg")
public class MsgController extends JsonController<Msg>
{

	/***
	 * 使用页面缓存 注意：经常变动的不能使用缓存 与权限相关的 不能用页面缓存 可使用 sql 缓存
	 * 
	 */
//	@Before(value = { CacheInterceptor.class })
	public void list()
	{
		setAttr("list", Msg.dao.list(getAid()));

		render(UrlConfig.VIEW_INDEX_MSG);

	}

	@Before(value = { EvictInterceptor.class, MsgValidator.class })
	public void add()
	{
		renderJsonResult(getModel().set("aid",getAid()).set("uid", user.getId()).saveAndDate());
	}

}
