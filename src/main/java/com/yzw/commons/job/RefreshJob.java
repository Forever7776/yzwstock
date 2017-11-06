package com.yzw.commons.job;

import com.jfinal.log.Logger;
import com.jfinal.plugin.ehcache.CacheKit;
import com.yzw.base.config.Consts;
import com.yzw.system.model.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by liuyingjun on 2015/8/28.
 * 定时检查Token失效
 */
public class RefreshJob implements Job {
    public static final Logger logger = Logger.getLogger(RefreshJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("token刷新处理中……");
        Map<String,Object> tokenMap = CacheKit.get(Consts.PF_API_DATA, Consts.LOGIN_TOKEN);
        if(tokenMap!=null){
            Iterator iterator = tokenMap.entrySet().iterator();
            while (iterator.hasNext()){

                Map.Entry tokenObj = (Map.Entry)iterator.next();
                long expiredTime =(long)tokenObj.getValue();
                logger.info("token失效时间："+ expiredTime);
                if(expiredTime<System.currentTimeMillis()){
                    iterator.remove();
                    logger.info("移除失效token成功："+expiredTime);
                }
            }
        }

    }
}
