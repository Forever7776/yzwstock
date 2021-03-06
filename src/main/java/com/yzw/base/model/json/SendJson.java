package com.yzw.base.model.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfinal.plugin.activerecord.Record;
import com.yzw.base.jfinal.ext.model.Model;

public class SendJson
{
	public int code = 200;
	/**
	 *  错误描述可写 可不写 可集中在 客户端 判断 code 得出
	 */
	public String msg ; 

	public Map data = new HashMap();

	public SendJson(Model model)
	{
		if (model != null) this.data = model.getAttrs();

	}

	public SendJson(String key, List list)
	{
		 setData(key, list);
	}

	public SendJson()
	{

	}

	public SendJson(int code)
	{

		this.code = code;
	}

	public String toJson()
	{
		if(data.size()==0)data=null;
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(this);
	}
	
	@Override
	public String toString() {
		return toJson();
	}

	public SendJson setData(String key, Model m)
	{
		this.data.put(key, m.getAttrs());

		return this;
	}
	
	public SendJson put(String key,Object value){
		this.data.put(key, value);
		
		return this;
	}

	public void setData(String key, List list)
	{
		if (list == null) return   ;
		List<Map> attr = new ArrayList<Map>();

		for (Object o : list)
		{
			if (o instanceof Model) attr.add(((Model) o).getAttrs());
			if(o instanceof Record ) attr.add(((Record) o).getColumns());
			
		}
		data.put(key, attr);
		
		if(attr.size()==0) data.put(key, list);

	}

	 
}
