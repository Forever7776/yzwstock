package com.yzw.commons.service;

import javax.mail.MessagingException;

import com.yzw.base.util.Eml;
import org.apache.commons.lang3.StringUtils;

public class EmailService
{

	public static void sendModifyPwdEmail(String email)
	{

		try
		{
			if(StringUtils.isEmpty(email))return;
			
			Eml eml = new Eml("smtp.126.com", "wangshuaiby@126.com", "wangshuaiby@126.com", "231566qq");
			eml.addTo(email);
			eml.setSubject("密码修改提示");
			eml.setBody("你最近修改了密码 如非本人操作 请及时 联系管理员 By Jfinal Authority");
			eml.send();
		} catch (MessagingException e)
		{
			e.printStackTrace();
		}

	}
	
	
	
	public static void sendValidatorEmail(String email,String val)
	{

		try
		{
			if(StringUtils.isEmpty(email))return;
			
			Eml eml = new Eml("smtp.126.com", "wangshuaiby@126.com", "wangshuaiby@126.com", "231566qq");
			eml.addTo(email);
			eml.setSubject("你正在注册用户  输下面的验证码完成验证");
			eml.setBody("验证码为: "+val);
			eml.send();
		} catch (MessagingException e)
		{
			e.printStackTrace();
		}

	}

}
