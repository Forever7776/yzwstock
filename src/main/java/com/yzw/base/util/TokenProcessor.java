package com.yzw.base.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


import sun.misc.BASE64Encoder;


//令牌生产器
public class TokenProcessor {
    private TokenProcessor(){}
	private static TokenProcessor instance = new TokenProcessor();
	public static TokenProcessor getInstance(){
		return instance;
	}
	public String generateTokeCode(String account){
		String value = System.currentTimeMillis()+new Random().nextInt()+"|"+account;
		//获取数据指纹，指纹是唯一的
		try {
			//Base64编码
			BASE64Encoder be = new BASE64Encoder();
			be.encode(value.getBytes());
			return be.encode(value.getBytes());//制定一个编码
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}