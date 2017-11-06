package com.yzw.admin.validator;


import com.jfinal.core.Controller;
import com.yzw.admin.controller.AdminUrlConfig;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.Validator;
import com.yzw.base.util.RSA;
import org.apache.commons.lang3.StringUtils;

public class AdminLoginValidator extends Validator
{


	@Override
	protected void validate(Controller c)
	{
		validateRequiredString("key",   "请重新登录");

		String key = c.getPara("key");
		if (StringUtils.isNotEmpty(key))
		{
			String [] result = RSA.decryptUsernameAndPwd(key);

			if (result==null) addError( ERROR_MSG, "用户名或密码不能为空");
		}
		
		

	}

	@Override
	protected void handleError(Controller c)
	{
		c.forwardAction(AdminUrlConfig.LOGIN);
	}

}
