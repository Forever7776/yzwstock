package com.yzw.commons.thread;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.ehcache.CacheKit;
import com.yzw.stock.model.Product;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by liuyj on 2015/9/7.
 */
public class SysnStockDateToWxPfThreadRun implements Runnable {
    private static final Logger logger = Logger.getLogger(SysnStockDateToWxPfThreadRun.class);
    private static String account_id;
    private static JSONObject paramObject;
    private static Controller cl;

    /**
     *
     * @param account_id
     * @param paramObject
     */
    public SysnStockDateToWxPfThreadRun(String account_id, JSONObject paramObject, Controller cl){
        this.account_id=account_id;
        this.paramObject=paramObject;
        this.cl=cl;
    }

    @Override
    public void run() {
        if(paramObject!=null&& StringUtils.isNotBlank(paramObject.getString("mcode"))){
            //请求接口处理……
            switch (paramObject.getString("mcode")){
                case "synGoodsStockNums":
                break;
            }
        }
    }
}
