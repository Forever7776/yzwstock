package com.yzw.system.controller;


import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.Consts;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ShiroExt;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.base.shiro.ShiroCache;
import com.yzw.system.model.Role;
import com.yzw.system.model.User;
import com.yzw.system.validator.RoleValidator;

@ControllerBind(controllerKey = "/system/role", viewPath = UrlConfig.SYSTEM)
public class RoleController extends Controller<Role>
{

	public void list()
	{
		renderJson(Role.dao.list(this.getAid()));
	}
	
	

	public void tree()
	{
		Integer pid = getParaToInt("id");
		Integer passId = getParaToInt("passId");
		renderJson(Role.dao.getTree(getAid(),pid, passId));

	}

	public void grant()
	{
		Role role = getModel();
		role.set("aid",getAid());
		String res_ids = getPara("res_ids");
		renderJsonResult(Role.dao.batchGrant(role.getId(), res_ids));

		ShiroCache.clearAuthorizationInfoAll();

	}

	@Override
	@Before(value = { RoleValidator.class })
	public void add()
	{
		renderJsonResult(getModel().set("aid",getAid()).save());
	}

	@Override
	@Before(value = { RoleValidator.class })
	public void edit()
	{
		Role role = getModel();

		if (role.getId() == role.getPid()) renderJsonError("父节点不能为自己");
		else if (Role.dao.pidIsChild(role.getId(), role.getPid())) renderJsonError("父节点不能为子节点");
		else renderJsonResult(role.update());

	}

	public void delete()
	{
		int id = getParaToInt("id");

		for (Role r : Role.dao.getRole(getAid()))
		{
			if (r.getId() == id)
			{
				renderJsonError("无法删除 自己的角色");
				return;
			}
		}

		if (id == 1) renderJsonError("admin 无法删除");
		else renderJsonResult(Role.dao.deleteByIdAndPid(id));
	}

}
