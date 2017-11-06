package com.yzw.base.jfinal.ext.ctrl;


import com.yzw.base.model.easyui.DataGrid;

public class EasyuiController<T> extends Controller<T>
{


	public DataGrid<T> getDataGrid()
	{
		DataGrid<T> dg = new DataGrid<T>();
		dg.sortName = getPara("sort", "");
		dg.sortOrder = getPara("order", "");
		dg.page = getParaToInt("page", 1);
		dg.total = getParaToInt("rows", 15);

		return dg;
	}


}
