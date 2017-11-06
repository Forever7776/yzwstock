package com.yzw.system.validator;


import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;
import com.yzw.system.model.User;
import org.apache.commons.lang3.StringUtils;

public class UserValidator extends Validator
{

	@Override
	protected void validate(Controller c)
	{
		super.validate(c);

		if (!isEmpty("repwd")) validateString("user.pwd", 5, 100, "密码不能为空 并且在5 到100个字符");
		else
		{
			validateString("user.name", 1, 20, "请输入名称,且不能超过20个字符");
			validateString("user.des",  0, 100, "请输入描述,且不能超过100个字符");
			validateEmail("user.email", false);
			if (StringUtils.isEmpty(c.getPara("user.id")) && User.dao.checkNameExist(c.getPara("user.name"))) addError(c.getPara("user.name")+"用户名已存在，请使用其它用户名");
		}
	}

}
