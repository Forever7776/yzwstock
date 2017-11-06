package com.yzw.base.jfinal.ext.model;

import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;

import java.util.List;



public class EasyuiModel<M extends Model<M>> extends Model<M>
{
    private static final long serialVersionUID = -7162337934972053871L;
    /***
     * 自定义sql
     *
     * @param sql
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<M> listByDataGrid(String sql, DataGrid<M> dg, Form f)
    {
        String sql_all = sql + f.getWhereAndLimit(dg);
        List<M> list = find(sql_all);
        dg.rows = list;
        dg.total = (int) getCount(sql_all);

        return dg;
    }

    /***
     * 自定义sql(复杂查询条件使用)
     * 刘英俊
     * @param select
     * @param sqlExceptSelect
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<M> listByDataGrid(String select, String sqlExceptSelect, DataGrid<M> dg, Form f)
    {
        String sql_all = select+" "+sqlExceptSelect + f.getWhereAndLimit(dg);
        List<M> list = find(sql_all);
        dg.rows = list;
        dg.total = (int) getCountByMoreFrom(sqlExceptSelect+f.getWhereAndSort(dg));

        return dg;
    }

    public List<M> list(DataGrid dg, Form f)
    {
        return list(f.getWhereAndLimit(dg));
    }


    /***
     * 自定义sql
     *
     * 这个方法无法 自动 获取 过滤 where 了
     *
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<M> listByDataGrid(DataGrid<M> dg, Form f, String where, Object... params)
    {
        List<M> list = find(" select *from " + tableName + " " + where, params);
        dg.rows = list;
        dg.total = (int) getCount(" select *from " + tableName + " " + where, params);

        return dg;
    }

    /***
     * 自定义sql
     *
     * @param sql
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<M> listByDataGridXml(String sql, DataGrid<M> dg, Form f)
    {
        List<M> list = find(sql(sql) + f.getWhereAndLimit(dg));
        dg.rows = list;
        dg.total = (int) getCount(sql(sql) + f.getWhereAndSort(dg));

        return dg;
    }

    /***
     * 自定义sql
     *
     * @param sql
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<M> listByDataGridXml(String sql, DataGrid<M> dg, Form f, String where, Object... params)
    {

        List<M> list = find(sql(sql) + f.getWhere() + where + f.sort(dg.sortName, dg.sortOrder) + f.limit(dg.page, dg.total), params);
        dg.rows = list;
        dg.total = (int) getCount(sql(sql) + f.getWhere() + where, params);

        return dg;
    }

    /***
     * 直接插 自动删除 最简单的sql
     *
     * @param dg
     * @param f
     * @return
     */
    public DataGrid<M> listByDataGrid(DataGrid<M> dg, Form f)
    {
        List<M> list = list(f.getWhereAndLimit(dg));
        dg.rows = list;
        dg.total = (int) getCount("select * from " + tableName + " " + f.getWhereAndSort(dg));

        return dg;
    }
}
