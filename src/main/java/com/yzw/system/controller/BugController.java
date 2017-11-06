package com.yzw.system.controller;


import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.Form;
import com.yzw.system.model.Bug;
import com.yzw.system.validator.BugValidator;

@ControllerBind(controllerKey = "/system/bug", viewPath = UrlConfig.SYSTEM)
public class BugController extends EasyuiController<Bug>
{

	public void list()
	{
		renderJson(Bug.dao.list(getDataGrid(), getFrom(null)));
	}

	public void status()
	{

		renderJsonResult(getModel().updateAndModifyDate());

	}

	@Before(value = { BugValidator.class })
	public void add()
	{
		renderJsonResult(getModel().saveAndCreateDate());

	}

	@Before(value = { BugValidator.class })
	public void edit()
	{
		renderJsonResult(getModel().updateAndModifyDate());

	}

	public void delete()
	{
		renderJsonResult(Bug.dao.deleteById(getPara("id")));
	}

}
