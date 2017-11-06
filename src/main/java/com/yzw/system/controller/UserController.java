package com.yzw.system.controller;


import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.shiro.ShiroCache;
import com.yzw.commons.service.EmailService;
import com.yzw.system.model.User;
import com.yzw.system.validator.UserValidator;
import org.apache.commons.lang3.StringUtils;

@ControllerBind(controllerKey = "/system/user", viewPath = UrlConfig.SYSTEM)
public class UserController extends EasyuiController<User>
{
	public void list()
	{
		renderJson(User.dao.listByDataGrid(getDataGrid(), getFrom(User.dao.tableName)));
	}
	
	
	public void select()
	{
		renderJson(User.dao.list(getDataGrid(), getFrom(User.dao.tableName)));
	}


	@Override
	public void delete()
	{
		renderJsonResult(User.dao.deleteById(getPara("id")));
	}

	public void freeze()
	{
		renderJsonResult(User.dao.changeStaus(getParaToInt("id"), getParaToInt("status")));
	}

	public void batchDelete()
	{
		renderJsonResult(User.dao.batchDelete(getPara("ids")));
	}

	public void batchGrant()
	{

		Integer[] role_ids = getParaValuesToInt("role_ids");
		String ids = getPara("ids");

		renderJsonResult(User.dao.batchGrant(role_ids, ids));

		ShiroCache.clearAuthorizationInfoAll();

	}

	@Before(value = { UserValidator.class })
	public void add()
	{
		renderJsonResult(getModel().encrypt().set("aid",this.getAid()).saveAndDate());
	}

	@Override
	@Before(value = { UserValidator.class })
	public void edit()
	{
		
		renderJsonResult(getModel().update());

	}

	@Before(value = { UserValidator.class })
	public void pwd()
	{
		renderJsonResult(getModel().encrypt().update());
		
		
		//send eamil
		User user = User.dao.findById(getModel().getId());
		if (StringUtils.isNotEmpty(user.getStr("email"))) ;
		new EmailService().sendModifyPwdEmail(user.getStr("email"));

	}

	public void grant()
	{
		Integer[] role_ids = getParaValuesToInt("role_ids");
		renderJsonResult(User.dao.grant(role_ids, getModel().getId()));
		ShiroCache.clearAuthorizationInfoAll();

	}

	public void getList(){
		renderJson(User.dao.getList(getAid()));

	}

}
