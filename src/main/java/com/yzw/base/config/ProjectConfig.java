package com.yzw.base.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.*;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.handler.FakeStaticHandler;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.ext.kit.ModelExt;
import com.jfinal.ext.kit.ModelFingerprint;
import com.jfinal.ext.kit.ModelKit;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.plugin.config.ConfigPlugin;
import com.jfinal.ext.plugin.quartz.QuartzPlugin;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.ext.plugin.sqlinxml.SqlInXmlPlugin;
import com.jfinal.ext.plugin.tablebind.AutoTableBindPlugin;
import com.jfinal.ext.plugin.tablebind.ModelInJar;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.SqlReporter;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.render.ViewType;
import com.yzw.base.jfinal.ext.ShiroExt;
import com.yzw.base.jfinal.ext.model.EasyuiModel;
import com.yzw.base.jfinal.ext.model.Model;
import com.yzw.base.jfinal.ext.xss.ACAOlHandler;
import com.yzw.base.jfinal.ext.xss.XssHandler;
import com.yzw.base.shiro.SessionHandler;
import org.beetl.core.GroupTemplate;
import org.beetl.ext.jfinal.BeetlRenderFactory;

import java.util.Properties;

/**
 * Created by luomhy on 2015/7/13.
 */
public class ProjectConfig extends JFinalConfig {
    private static Logger logger = Logger.getLogger(ProjectConfig.class);

    public boolean OPEN_SHIRO = true;
    public boolean OPEN_ADV =true; // 可设置隐藏  项目介绍等

    private Routes routes;
    private boolean isDev = isDevMode();

    private boolean isDevMode()
    {
        String osName = System.getProperty("os.name");
        return osName.indexOf("Windows") != -1;
    }

    @Override
    public void configConstant(Constants me) {
        new ConfigPlugin("config.properties").reload(false).start();

        me.setError404View("/page/error/404.html");
        me.setError401View("/page/error/401.html");
        me.setError403View("/page/error/403.html");
        me.setError500View("/page/error/500.html");

        me.setDevMode(isDev);

        //beetl
        BeetlRenderFactory beetlRenderFactory = new BeetlRenderFactory();
        me.setMainRenderFactory(beetlRenderFactory);
        GroupTemplate groupTemplate = beetlRenderFactory.groupTemplate;
        groupTemplate.registerFunctionPackage("so", new ShiroExt());
        me.setEncoding("UTF-8");
    }

    @Override
    public void configRoute(Routes me) {
        this.routes = me;
        me.add(new AutoBindRoutes().autoScan(false));
    }

    @Override
    public void configPlugin(Plugins me) {
        DruidPlugin dbPlugin = new DruidPlugin(ConfigKit.getStr("jdbc.url"), ConfigKit.getStr("jdbc.user"), ConfigKit.getStr("jdbc.pwd"));

        // 设置 状态监听与 sql防御
        WallFilter wall = new WallFilter();
        wall.setDbType(ConfigKit.getStr("dbType"));
        dbPlugin.addFilter(wall);
        dbPlugin.addFilter(new StatFilter());
        me.add(dbPlugin);
        logger.info("[init]druid 连接池启动#SUCCESS");


        AutoTableBindPlugin atbp = new AutoTableBindPlugin(dbPlugin);
        if (isDev) atbp.setShowSql(true);
        atbp.addExcludeClasses(ModelExt.class);
        atbp.addExcludeClasses(ModelFingerprint.class);
        atbp.addExcludeClasses(ModelInJar.class);
        atbp.addExcludeClasses(EasyuiModel.class);
        atbp.addExcludeClasses(Model.class);
        me.add(atbp);
        // sql记录
        SqlReporter.setLogger(true);
        logger.info("[init]AutoTableBindPlugin 表加载启动#SUCCESS");
        // add EhCache
        me.add(new EhCachePlugin());
        // add sql xml plugin
        me.add(new SqlInXmlPlugin());

        if (OPEN_SHIRO) me.add(new ShiroPlugin(this.routes));
        logger.info("[init]ShiroPlugin 权限插件加载#SUCCESS");
        me.add(new QuartzPlugin("job.properties"));
       /* RedisPlugin redisPlugin = new RedisPlugin("yzw","localhost");
        me.add(redisPlugin);*/
    }

    @Override
    public void configInterceptor(Interceptors me) {
        // shiro权限拦截器配置
        me.add(new ShiroInterceptor());
        me.add(new com.yzw.base.shiro.ShiroInterceptor());

        // 让 模版 可以使用session
        me.add(new SessionInViewInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {
        // xss 过滤
        me.add(new XssHandler("s"));
        // 伪静态处理
        /*me.add(new FakeStaticHandler());*/
        // 去掉 jsessionid 防止找不到action
        me.add(new SessionHandler());
        me.add(new DruidStatViewHandler("/druid"));

        me.add(new ContextPathHandler());
    }
}
