package com.yzw.admin.controller;

/**
 * Created by admin on 2015/9/25.
 */
public class AdminUrlConfig {
    public static final String LOGIN = "/loginView";
    public static final String BASE="/page/admin";


    //子模块
    public static final String ERROR=BASE+"/error";
    public static final String INDEX=BASE+"/index/admin-index.html";
    public static final String COMMON=BASE+"/common";

    public static final String VIEW_COMMON_LOGIN =COMMON+"/login.html";
    public static final String VIEW_COMMON_JUMP = COMMON+"/jump.html";

    public static final String VIEW_INDEX_MSG = INDEX+"/msg.html";

    public static final String VIEW_ERROR_401 = ERROR+"/401.html";

}
