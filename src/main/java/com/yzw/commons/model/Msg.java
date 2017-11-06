package com.yzw.commons.model;

import java.util.Collections;
import java.util.List;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.Model;

@TableBind(tableName = "index_msg")
public class Msg extends Model<Msg>
{
	private static final long serialVersionUID = -128801010211787215L;

	public static Msg dao = new Msg();

	public List<Msg> list(Integer aid)
	{
		String sql = sql("index.msg.list")+" where index_msg.aid=? "+" ORDER BY date desc limit 5;";
		List<Msg> list= dao.find(sql,aid);
		Collections.reverse(list);
		return list;
	}

}