package com.yzw.system.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.EasyuiModel;

/**
 * Created by liuyj on 2015/8/26.
 */
@TableBind(tableName = "admin_account")
public class Account extends EasyuiModel<Account> {
    public static final Account dao = new Account();


}
