package com.yzw.base.jfinal.ext.ctrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jfinal.ext.kit.JaxbKit;
import com.jfinal.ext.render.excel.PoiRender;
import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.Consts;
import com.yzw.base.jfinal.ext.ShiroExt;
import com.yzw.base.model.easyui.Form;
import com.yzw.base.util.Validate;
import com.yzw.system.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class Controller<T> extends com.jfinal.core.Controller {

	/**
	 * 默认 html 视图 一般用模版啥的
	 */
	public static final String VIEW_TYPE = ".html";

	ControllerBind controll;
	protected User user;

	public Controller()
	{
		user = ShiroExt.getSessionAttr(Consts.SESSION_USER);
		controll = this.getClass().getAnnotation(ControllerBind.class);
	}

	/**
	 * 获取 aid 区分使用用户
	 * @return
	 */
	public Integer getAid(){
		return user!=null?user.getInt("aid"):0;
	}

	/**
	 * 读取 inputstream string
	 *
	 * @return
	 */
	public String getInputStreamString()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(getRequest().getInputStream(), "utf-8"));
			String buffer = null;
			while ((buffer = br.readLine()) != null)
			{
				sb.append(buffer);
			}
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

	public <V> V getXml(Class<V> clazz)
	{
		return JaxbKit.unmarshal(getInputStreamString(), clazz);

	}

	/**
	 * fastjson转Map
	 *
	 * 然后 就可以设置 进 model setAttrs（Map<String,Object> map）
	 *
	 * @param json
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> fastjson2Map(String json) throws UnsupportedEncodingException
	{
		json = URLDecoder.decode(json, "utf-8");

		return (Map<String, Object>) JSON.parse(json.substring(json.lastIndexOf("{"), json.indexOf("}") + 1));
	}

	/**
	 * 通过传递 to参数来自动返回　HTML 页面 viewpath 需要设置 注解
	 */
	public void rh()
	{
		String viewpath = "";
		if (controll != null) viewpath = controll.viewPath();
		String to = getPara("to");
		keepPara();
		if (StringUtils.isNotEmpty(to)) render(viewpath + "/" + to + ".html");
	}

	/***
	 * 默认读取 注解来 转发到 约定的 视图
	 *
	 */
	public void index()
	{
		if (controll != null)
		{
			String key = controll.controllerKey();
			String viewpath = controll.viewPath();


			if (key.equals("/")) render(viewpath + "/index" + VIEW_TYPE);
			else if (!Validate.isEmpty(key, viewpath) && key.contains("/"))
			{
				String index = key.split("/")[key.split("/").length - 1] + VIEW_TYPE;
				render(viewpath + "/" + index);
			}
		}
	}

	public T getModel()
	{
		ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
		Class modelClass = (Class) pt.getActualTypeArguments()[0];

		return (T) super.getModel(modelClass);
	}

	public void renderExcel(List<?> data, String fileName, String[] headers)
	{

		PoiRender excel = PoiRender.me(data);
		excel.fileName(fileName);
		excel.headers(headers);
		excel.cellWidth(5000);
		render(excel);
	}

	public void renderJsonResult(boolean result)
	{
		if (result) renderJson200();
		else renderJson500();
	}
    public void renderJsonResult(boolean result,String msg)
    {
        if (result) renderJson200();
        else renderJsonError(msg);
    }

	public void renderJson500()
	{
		renderJson("{\"msg\":\"没有任何修改或 服务器错误\"}");
	}

	public void renderJsonError(String msg)
	{
		renderJson("{\"msg\":\" " + msg + " \"}");
	}

	public void renderJson200()
	{
		renderJson("{\"code\":200}");
	}

	public void delete() throws Exception
	{}

	public void list() throws Exception
	{}

	public void saveOrUpdate() throws Exception
	{}

	public void add() throws Exception
	{

	}

	public void edit() throws Exception
	{

	}

	/***
	 * 通常用来组装 serach form
	 *
	 * tableName 用来 过滤多表
	 *
	 * 这是常用的几种
	 *
	 * @return
	 */
	public Form getFrom(String tableName)
	{

		return Form.getForm(tableName, this,"insertDate","insertDateStart","insertDateEnd","date", "dateStart", "dateEnd", "name", "title", "des", "msg", "url", "icon", "text", "pwd",
				"status", "type", "createdateStart", "createdateEnd", "modifydateStart", "modifydateEnd", "operation","code","aid");
	}

	public void forwardAction(String msg, String url)
	{

		setAttr("msg", msg);
		forwardAction(url);
	}

	public void render(String msg, String url)
	{
		setAttr("msg", msg);
		render(url);
	}

	/**
	 * render xml
	 *
	 * @param obj
	 */
	public void renderXml(Object obj)
	{

		renderText(JaxbKit.marshal(obj), "application/xml");
	}

	// public void renderBeetl(String view)
	// {
	//
	// render(new BeetlRender(BeetlRenderFactory.groupTemplate, view));
	// }

	public void renderTop(String url)
	{

		renderHtml("<html><script> window.open('" + url + "','_top') </script></html>");

	}

	/***
	 *
	 * 什么时候用 gson 呐
	 *
	 * 如果是 原生的 List<Model> 直接返回即可 用 renderJson
	 *
	 *
	 * @param obj
	 */
	public void renderGson(Object obj)
	{

		renderJson(new Gson().toJson(obj));
	}

	/***
	 *
	 * 好像有个问题
	 *
	 * @param obj
	 */
	public void renderFastJson(Object obj)
	{
		renderJson(JSON.toJSONString(obj));
	}


	/**
	 * 判断当前用户是否为超级管理员-admin
	 */
	public boolean isSuperAdmin(){
		List rolesname = this.user.getRolesName(this.user.getName());
		if(rolesname!=null&&rolesname.contains("admin")){
			return true;
		}else {
			return false;
		}
	}
 

}
