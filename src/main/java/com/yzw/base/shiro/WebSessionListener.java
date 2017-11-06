package com.yzw.base.shiro;


import com.yzw.base.util.L;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListenerAdapter;


public class WebSessionListener extends SessionListenerAdapter {

	
	
	@Override
	public void onExpiration(Session session) {
		
		L.i("onExpiration session +" + session);
		super.onExpiration(session);
	}
	
 

 
}
