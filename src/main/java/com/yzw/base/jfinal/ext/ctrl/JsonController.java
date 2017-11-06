package com.yzw.base.jfinal.ext.ctrl;

import com.yzw.base.jfinal.ext.model.Model;
import com.yzw.base.model.json.SendJson;

import java.util.List;


public class JsonController<T> extends Controller<T>
{
	public void renderJsonResult(boolean result) {
		if (result) renderJson200();
		else renderJson500();
	}

	
	public void renderJson500() {
		renderJson("{\"code\":500,\"msg\":\"没有任何修改或 服务器错误\"}");
	}

	public void renderJsonError(String msg) {
		renderJson("{\"code\":500,\"msg\":\" " + msg + " \"}");
	}

	public void renderJson200() {
		renderJson("{\"code\":200}");
	}

	
	public SendJson getJsonObject(){
		
		return new SendJson();
	}
	

	public void sendJson(String key, List list)
	{

		renderJson(new SendJson(key, list).toJson());
	}
	
	public void sendJson(String key,Object obj){
		
		renderJson(new SendJson().put(key, obj).toJson());
	}

	public void sendJson(int code, SendJson result)
	{
		if (code == 200) renderJson(result.toJson());
		else sendJson(code);
	}

	public void sendJson(SendJson result)
	{
		renderJson(result.toJson());
	}

	public void sendJson()
	{
		renderJson(new SendJson().toJson());
	}

	
	public void sendJson(boolean result)
	{
		int code =200;
		if(!result)code=500;
		sendJson(code);
	}
	
	public void sendJson(int code)
	{

		renderJson(new SendJson(code).toJson());
	}

	public void sendJson(Model m)
	{

		renderJson(new SendJson(m).toJson());
	}
	
	
	
	
 
	

}
