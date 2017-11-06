package com.yzw.system.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.alibaba.fastjson.JSONObject;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.yzw.base.config.Consts;
import com.yzw.base.jfinal.ext.ListUtil;
import com.yzw.base.jfinal.ext.ShiroExt;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import com.yzw.base.model.easyui.Tree;
import com.yzw.base.shiro.ShiroCache;
import com.yzw.base.util.Sec;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

@TableBind(tableName = "system_user")
public class User extends EasyuiModel<User>
{
	private static final long serialVersionUID = -7615377924993713398L;

	public static User dao = new User();

	/***
	 * 隐藏 id 1
	 */
	public DataGrid<User> listByDataGrid(DataGrid<User> dg, Form f)
	{
		User now = ShiroExt.getSessionAttr(Consts.SESSION_USER);
		if(now!=null) f.addFrom(f,"aid-i",now.getInt("aid")+"");//添加  aid 获取数据

		dg = super.listByDataGrid(sql("system.user.list"), dg, f);
		Iterator<User> list = dg.rows.iterator();


		while (list.hasNext())
		{
			User u = list.next();
			List<Role> role = Role.dao.getRole(u.getId());
			u.put("role_ids", ListUtil.listToString(role, "id"));
			u.put("role_names", ListUtil.listToString(role, "name"));
			if (u.getId() == 1 && now.getId() != 1) list.remove();
		}

		return dg;
	}

	public List<String> getRolesName(String loginName)
	{
		return getAttr(sql("system.role.getRolesName"), "name", loginName);
	}

	public boolean grant(Integer[] role_ids, Integer userId)
	{
		boolean result = Db.deleteById("system_user_role", "user_id", userId);

		if (role_ids == null) return result;

		Object[][] params = ListUtil.ArrayToArray(userId, role_ids);
		result = Db.batch("insert into system_user_role(user_id,role_id)  values(?,?)", params, role_ids.length).length > 0;

		ShiroCache.clearAuthorizationInfoAll();

		return result;
	}

	public User encrypt()
	{
		String pwd = this.getPwd();
		if(StringUtils.isEmpty(pwd))pwd="123456";
		
		this.set("pwd", DigestUtils.md5Hex(pwd));
		return this;
	}

	public boolean batchGrant(Integer[] role_ids, String uids)
	{
		boolean result = Db.update("delete from system_user_role where user_id in (" + uids + ")") > 0;

		if (role_ids == null) return result;

		Object[][] params = ListUtil.ArrayToArray(uids, role_ids);

		result = Db.batch("insert into system_user_role(user_id,role_id)  values(?,?)", params, params.length).length > 0;

		ShiroCache.clearAuthorizationInfoAll();

		return result;
	}

	public boolean changeStaus(Integer id, Integer status)
	{
		if(status==null)return false;
		else status=1;
		return dao.update("status",status,id) ;

	}

	public List<Tree> getTree(Integer aid,Integer passId)
	{
		// 根据用户角色来获取 列表
		List<Tree> trees = new ArrayList<Tree>();

		for (User user : dao.list("where aid=? ",aid))
		{
			if(user.getId().equals(passId)) continue;
			Tree tree = new Tree(user.getId(), null, user.getName(), "box", user, false);
			trees.add(tree);
		}

		return trees;
	}

	public List<User> getList(Integer aid){
		List<User> userlist = dao.list("where aid=? ",aid);
		List<User> newuserlist = new ArrayList<>();
		for(User user:userlist){
			User u = new User();
			u.put("id",user.getInt("id"));
			u.put("text",user.get("name"));
			newuserlist.add(u);
		}
		return  newuserlist;
	}

	
	
}
