package com.yzw.commons.thread;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;

/**
 * Created by liuyj on 2015/9/16.
 */
public class ThreadStart {
    public static void SysnStockDateToWxPfThreadRun(String pf_account_id,JSONObject json,Controller cl){
        Thread thread = new Thread(new SysnStockDateToWxPfThreadRun(pf_account_id,json,cl));
        thread.start();
    }
}
