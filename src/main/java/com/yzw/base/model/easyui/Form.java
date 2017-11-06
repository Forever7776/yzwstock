package com.yzw.base.model.easyui;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.yzw.base.util.Txt;
import org.apache.commons.lang3.StringUtils;

import com.jfinal.core.Controller;

/***
 * 
 * 基于约定 其他人不要使用
 * 
 * 可用来生成 where
 * 
 * @author 12
 * 
 */
public class Form
{

	// public Map<String, String> pramMap = new HashMap<String, String>();

	public Map<String, String> fromMap = new HashMap<String, String>();

	public Set<String> fuzzySerach = new HashSet<String>();
	public Set<String> integerValue = new HashSet<String>();

	public Set<String> inValue = new HashSet<String>();
	public Set<String> ninValue = new HashSet<String>();

	private String tableName = "";

	String where = " where 1=1 ";

	public Form()
	{
		fuzzySerach.add("name");
		fuzzySerach.add("title");

		integerValue.add("status");
		integerValue.add("type");
		integerValue.add("operation");

	}

	public Form(String tableName)
	{
		this();
		if (StringUtils.isNotEmpty(tableName)) this.tableName = tableName;
	}

	public static Form getForm(String tableName, Controller c, String... params)
	{
		Form form = new Form(tableName);

		for (String key : params)
		{
			form.fromMap.put(key, c.getPara(key, null));
		}

		Enumeration<String> names = c.getParaNames();

		while (names.hasMoreElements())
		{
			String name = names.nextElement();
			if (name.contains(".")) form.addFrom(form, name, c.getPara(name, null));
			else if(name.contains("-")) form.addFrom(form, name, c.getPara(name,null));
		}

		return form;
	}

	public void addFrom(Form form, String name, String value)
	{
		if (name.endsWith("-*"))
		{
			String newName = Txt.split(name, "-*")[0];
			form.fuzzySerach.add(newName);
			form.fromMap.put(newName, value);
		}
		else if (name.endsWith("-i"))
		{
			String newName = Txt.split(name, "-i")[0];
			form.integerValue.add(newName);
			form.fromMap.put(newName, value);
		}
		else if (name.endsWith("-in"))
		{
			String newName = Txt.split(name, "-in")[0];
			form.inValue.add(newName);
			form.fromMap.put(newName, value);
		}
		else if (name.endsWith("-nin"))
		{
			String newName = Txt.split(name, "-nin")[0];
			form.ninValue.add(newName);
			form.fromMap.put(newName, value);
		}
		else if (name.endsWith("-s"))
		{
			String newName = Txt.split(name, "-s")[0];
			form.fromMap.put(newName, value);
		}
	}

	/**
	 * 手动添加where
	 * 
	 * @param where
	 * @return
	 */
	public String addWhere(String where)
	{

		return this.where += " " + where;
	}

	//
	// /**
	// * 手动添加where 到getwhere的基础上
	// *
	// * @param where
	// * @return
	// */
	// public String getWhere(String where)
	// {
	// where += getWhere()+where;
	// return where;
	// }

	public String getFromParm(String key)
	{

		return fromMap.get(key);
	}

	public String setFromParm(String key, String value)
	{
		return fromMap.put(key, value);
	}

	public String getWhereAndSort(DataGrid dg)
	{
		if (getWhere().contains("date")) dg.sortOrder = "asc";

		return getWhere() + sort(dg.sortName, dg.sortOrder);

	}

	/***
	 * '%Y-%m-%d'
	 * 
	 * @return
	 */
	public String groupDate()
	{

		return " GROUP BY DATE_FORMAT(date,'%Y-%m-%d')";
	}

	public String getWhereGroupDate()
	{

		return getWhere() + groupDate();
	}
	
	public String getCountSql(String string)
	{
		
		
		return null;
	}

	/***
	 * 设置自己的where
	 * 
	 * @param dg
	 * @param where
	 * @return
	 */
	public String getWhereAndLimit(DataGrid dg, String where)
	{
		int page = dg.page;
		if (page > 0) page -= 1;
		return where + limit(page, dg.total);
	}

	public String getWhereAndLimit(DataGrid dg)
	{
		int page = dg.page;
		return getWhereAndSort(dg) + limit(page, dg.total);
	}

	public String getWhere()
	{
		if (StringUtils.isNotEmpty(tableName) && !tableName.contains(".")) tableName += ".";
		where = " where 1=1 ";

		for (String key : fromMap.keySet())
		{
			String value = fromMap.get(key);
			String namespace =tableName;
			
			if(key.contains(".")) namespace="";
			
			if (StringUtils.isNotBlank(value))
			{
				if ("dateStart".equals(key)) gteq(namespace + "date", quotation(value));
				else if ("dateEnd".equals(key)) lteq(namespace + "date", quotation(value));
				else if ("insertDateStart".equals(key)) gteq(namespace + "insert_date", quotation(value));
				else if ("insertDateEnd".equals(key)) lteq(namespace + "insert_date", quotation(value));
				else if ("createdateStart".equals(key)) gteq(namespace + "createdate", quotation(value));
				else if ("createdateEnd".equals(key)) lteq(namespace + "createdate", quotation(value));
				else if ("modifydateStart".equals(key)) gteq(namespace + "modifydate", quotation(value));
				else if ("modifydateEnd".equals(key)) lteq(namespace + "modifydate", quotation(value));

				else if (fuzzySerach.contains(key)) like(namespace + key, value);
				else if (integerValue.contains(key)) where += " and " + namespace + key + "=" + value + " ";
				else if (inValue.contains(key)) where += " and " + namespace + key + " in (" + value + ")";
				else if (ninValue.contains(key)) where += " and " + namespace + key + " not in (" + value + ")";
				else where += " and " + namespace + key + "=" + quotation(value) + " ";

			}
		}
		return where;
	}

	public String sort(String sortName, String sortOrder)
	{
		if (StringUtils.isEmpty(sortName)) return "";
		else return " order by " + sortName + " " + sortOrder;
	}

	public String limit(int page, int size)
	{
		if (page > 0) page -= 1;
		return " limit " + size * page + "," + size;
	}

	public String limit(int size)
	{

		return " limit " + size;
	}

	/***
	 * >=
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String gteq(String key, String value)
	{

		return where += " and " + key + ">=" + value;
	}

	/***
	 * <=
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String lteq(String key, String value)
	{

		return where += " and " + key + "<=" + value;
	}

	/***
	 * >
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String gt(String key, String value)
	{

		return where += " and " + key + ">" + value;
	}

	/***
	 * <
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String lt(String key, String value)
	{

		return where += " and " + key + "<" + value;
	}

	public String quotation(String value)
	{

		return "'" + value + "'";
	}

	public String like(String key, String value)
	{

		return where += " and " + key + " like '%" + value + "%'";
	}



}
