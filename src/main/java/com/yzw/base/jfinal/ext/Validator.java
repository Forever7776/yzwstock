package com.yzw.base.jfinal.ext;

import com.jfinal.core.Controller;
import org.apache.commons.lang3.StringUtils;

public abstract class Validator extends com.jfinal.validate.Validator
{

	/***
	 * 默认 error msg
	 */
	public static final String ERROR_MSG = "msg";

	private Controller controller;

	protected void validate(Controller c)
	{

		this.controller = c;

	}


	protected boolean isEmpty(String key){

		return StringUtils.isEmpty(controller.getPara(key));
	}

	@Override
	protected void handleError(Controller c)
	{
		c.renderJson(ERROR_MSG, c.getAttr(ERROR_MSG));

	}

	protected void addError(String errorMessage)
	{
		super.addError(ERROR_MSG, errorMessage);
	}

	protected void validateRequiredString(String field, String errorMessage)
	{
		super.validateRequiredString(field, ERROR_MSG, errorMessage);
	}

	protected void validateString(String field, int minLen, int maxLen, String errorMessage)
	{
		super.validateString(field, minLen, maxLen, ERROR_MSG, errorMessage);
	}

	protected void validateEmail(String field, boolean notBlank)
	{
		if (notBlank) super.validateEmail(field, ERROR_MSG, "email 格式错误 请重新输入");
		else
		{
			String value = controller.getPara(field);
			if (StringUtils.isNotEmpty(value)) super.validateEmail(field, ERROR_MSG, "email 格式错误 请重新输入");
		}

	}

	protected void validateInteger(String field, int min, int max, String errorMessage)
	{
		super.validateInteger(field, min, max, ERROR_MSG, errorMessage);
	}

	protected void validateString(String field, boolean notBlank, int minLen, int maxLen, String errorMessage)
	{
		if(notBlank){
			String value = controller.getPara(field);
			if(StringUtils.isNotBlank(value)){
				super.validateString(field, minLen, maxLen, ERROR_MSG, errorMessage);
			}
		}else
			super.validateString(field, minLen, maxLen, ERROR_MSG, errorMessage);
	}

}
