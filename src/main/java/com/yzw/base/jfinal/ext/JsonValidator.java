package com.yzw.base.jfinal.ext;

import com.jfinal.core.Controller;
import com.yzw.base.model.json.SendJson;

public class JsonValidator extends Validator
{
	protected void addError(int code )
	{
		super.addError("code", code+"");

	}

	@Override
	protected void handleError(Controller c)
	{
		c.renderJson(new SendJson(Integer.parseInt((String)c.getAttr("code"))) .toJson());

	}

}
