package com.yzw.system.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

public class RoleValidator extends Validator
{

	@Override
	protected void validate(Controller c)
	{
		validateString("role.name", 1, 20, "名称不能为空 并且不能超过20个字符");
		validateString("role.des",false, 0, 100, "描述不能超过100个字符");
		validateInteger("role.seq", 0, 1000, "序列值过大");
	}

	
}
