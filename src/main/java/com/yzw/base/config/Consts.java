package com.yzw.base.config;

public class Consts
{

	public static final String SESSION_USER = "user";
	
	public static final String SESSION_EAMIL_USER="email_val";

	/***
	 * 分布式session开关 请在redis.properties 配置ip和端口
	 */
	public static boolean OPEN_REDIS = false;

    public static String PF_API_DATA = "pf_api_data";

    public static String LOGIN_TOKEN = "login_token";

    public static  Integer[] ADMIN_ROLE_ID = {1};
    public static  Integer[] FENCANG_MANAGER_ROLE_ID = {5};


}
