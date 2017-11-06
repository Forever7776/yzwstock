package com.yzw.system.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;

@TableBind(tableName = "system_bug")
public class Bug extends EasyuiModel<Bug>
{
	private static final long serialVersionUID = 3706516534681611550L;

	public static final Bug dao = new Bug();




}
