package com.yzw.stock.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.yzw.base.config.Consts;
import com.yzw.base.jfinal.ext.ListUtil;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.yzw.base.shiro.ShiroCache;
import com.yzw.base.util.TokenProcessor;
import com.yzw.base.util.serial.GenerateSerialNo;
import com.yzw.stock.model.*;
import com.yzw.system.model.Account;
import com.yzw.system.model.Res;
import com.yzw.system.model.Role;
import com.yzw.system.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.util.*;


/**
 * Created by liuyj on 2015/8/25.
 */
public class WeixinPlatformApiService {
    public static final int expires_in = 30*60*1000;//毫秒-设置缓存时间为30分钟
    public Controller cl;
    public WeixinPlatformApiService(Controller controller){
        this.cl = controller;
    }
    private static final Logger logger = Logger.getLogger(WeixinPlatformApiService.class);

    /**
     * 登录并返回token
     * @param data
     * @param paramJson
     * @return
     */
    public JSONObject login(JSONObject data,JSONObject paramJson){

        if (StringUtils.isNotBlank(paramJson.getString("account")) && StringUtils.isNotBlank(paramJson.getString("pwd"))) {
            Account account = Account.dao.findFirst("select * from admin_account where name = ? and pwd = ?",paramJson.getString("account"),paramJson.getString("pwd"));
            if(account!=null){
                String token = TokenProcessor.getInstance().generateTokeCode(paramJson.getString("account"));
                Map<String,Object> tokenMap = CacheKit.get(Consts.PF_API_DATA,Consts.LOGIN_TOKEN);
                if(tokenMap==null){
                    tokenMap = new HashMap<>();
                }
                tokenMap.put(token,new DateUtils().addMilliseconds(new Date(),expires_in+1000*5).getTime());
                CacheKit.put(Consts.PF_API_DATA,Consts.LOGIN_TOKEN,tokenMap);

                if (StringUtils.isNotBlank(token)){
                    data.put("result", true);
                    data.put("stock_token", token);
                    data.put("expires_in", expires_in);
                    data.put("msg", "登录成功");
                }
            }else{
                data.put("msg", "登录失败，账户与密码验证不通过！");
            }
        } else {
            data.put("msg", "登录失败，参数错误！");
        }
        return data;
    }

    /**
     * 检查token是否合法有效
     * @param token
     * @return boolean
     */
    public boolean check(String token){
        Map<String,Object> tokenMap =  CacheKit.get(Consts.PF_API_DATA,Consts.LOGIN_TOKEN);
        if(tokenMap!=null){
            Long expirestime =(Long)tokenMap.get(token);
            if(expirestime!=null&&expirestime>System.currentTimeMillis()){
                return true;
            }else{
                return false;
            }
        }
        //库存系统因重启等原因session丢失,暂时先放过
        return true;
    }

    /**
     * Token解码
     * @param token
     * @return
     */
    public String tokenDecode(String token){
        String account = null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] d = decoder.decodeBuffer(token);
            account = new String(d);
            if(StringUtils.isNotBlank(account)){
                account = account.split("\\|")[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    @Before(Tx.class)
    public JSONObject newAccount(JSONObject data,JSONObject paramJson){
        if(StringUtils.isNotBlank(paramJson.getString("account"))&&StringUtils.isNotBlank(paramJson.getString("pwd"))&&StringUtils.isNotBlank(paramJson.getString("nickname"))){
            //1、创建库存系统账号
            Account account = new Account();
            account.set("name",paramJson.getString("account"));
            account.set("pwd",paramJson.getString("pwd"));
            account.set("status",1);
            account.set("nickname",paramJson.getString("nickname"));
            account.set("wx_aid",paramJson.getString("accountid"));

            boolean result = account.save();
            //默认初始化两个角色（admin,fencang）
            List<Role> roleList = Role.dao.find("select * from system_role where aid is null");
            for(Role role:roleList){
                List<Res> resList = Res.dao.getRes(role.getId());
                role.remove("id");
                role.set("aid", account.getId());
                role.save();
                Role.dao.grant(role.getId(), ListUtil.listToString(resList,"id"));
            }

            if(result){
                JSONObject o = new JSONObject();//返回的数据
                //1、创建账号总仓管理员
                User zonguser = new User();
                User existUser = User.dao.findFirst("select * from system_user where name = ?",paramJson.getString("account"));
                if(existUser==null){
                    zonguser.set("name",paramJson.get("account"));//username
                    o.put("username", paramJson.get("account"));
                }else{
                    String username = paramJson.get("account")+"_"+GenerateSerialNo.createSerialUserCode();
                    zonguser.set("name", username);//username
                    o.put("username",username);
                }
                zonguser.set("aid",account.get("id"));
                //zonguser.set("name",paramJson.getString("account"));
                zonguser.set("pwd",paramJson.getString("pwd"));
                zonguser.set("status",1);
                zonguser.set("date",new Date());
                zonguser.set("wx_user_id",paramJson.getString("accountid"));
                zonguser.set("wx_user_type", 1);
                zonguser.save();
                Role role = Role.dao.findFirst("select * from system_role where aid = ? and is_admin = 1",account.getId());

                //授权为管理员
                User.dao.grant(new Integer[]{role.getId()}, zonguser.getId());
                ShiroCache.clearAuthorizationInfoAll();

                Stock zonstock = new Stock();
                zonstock.set("aid",account.get("id"));
                zonstock.set("name",paramJson.getString("account")+"总仓");
                zonstock.set("code", GenerateSerialNo.createSerialStockCode());
                zonstock.set("admin",zonguser.getInt("id"));
                zonstock.set("is_total",1);
                zonstock.set("address", paramJson.get("address"));
                zonstock.set("address_point", paramJson.get("address_point"));
                zonstock.set("level",1);
                zonstock.set("status",1);
                zonstock.set("insert_date",new Date());
                zonstock.set("last_date", new Date());
                zonstock.save();

                data.put("result", true);
                o.put("account",account.getStr("name"));
                o.put("aid",account.getInt("id"));
                o.put("pwd",paramJson.getString("pwd"));
                o.put("wx_userid",paramJson.getString("accountid"));
                o.put("stock_userid",zonguser.get("id"));
                data.put("data",o);
                data.put("msg","创建账号成功！");
            }
        }else{
            data.put("msg","参数错误，请检查参数！");
        }
        return data;
    }

    @Before(Tx.class)
    public JSONObject initStockData(Account account,JSONObject data,JSONObject paramJson){

        //2、如果有开通了分销商系统。创建分销商分仓账号
        JSONArray successfencang = new JSONArray();
        JSONArray fencangUserArray = paramJson.getJSONArray("data");
        for(int i = 0; i<fencangUserArray.size(); i++){
            JSONObject  fc = (JSONObject)fencangUserArray.get(i);
            User fencangUser = User.dao.findFirst("select * from system_user where wx_user_id = ?", fc.getString("wx_userid"));
            if(fencangUser==null){
                User existUser = User.dao.findFirst("select * from system_user where name = ?",fc.get("name"));
                fencangUser = new User();
                if(existUser==null){
                    fencangUser.set("wx_user_id",fc.getString("wx_userid"));
                    fencangUser.set("wx_user_type",2);
                    fencangUser.set("aid",account.get("id"));
                    fencangUser.set("name",fc.get("name"));//username
                    fc.put("username", fc.get("name"));
                }else{
                    String username = fc.get("name")+"_"+GenerateSerialNo.createSerialUserCode();
                    fencangUser.set("name", username);//username
                    fc.put("username",username);
                }

            }
            fencangUser.set("pwd",fc.get("pwd"));
            fencangUser.set("status",1);
            fencangUser.set("date",new Date());

            Boolean result = fencangUser.saveOrUpdate();

            //授权为分仓管理员
            Role role = Role.dao.findFirst("select * from system_role where aid = ? and is_admin = 0", account.getId());
            //授权为仓管理员
            User.dao.grant(new Integer[]{role.getId()}, fencangUser.getId());
            ShiroCache.clearAuthorizationInfoAll();
            //保存成功后，创建仓库
            if(result){

                Stock stock = Stock.dao.findFirst("select * from yzwstock_stock where admin = ?",fencangUser.getInt("id"));
                if(stock==null){
                    stock = new Stock();
                    stock.set("admin",fencangUser.getInt("id"));
                    stock.set("aid",account.get("id"));
                    stock.set("code", GenerateSerialNo.createSerialStockCode());
                    stock.set("is_total",0);
                    stock.set("level",1);
                    stock.set("status",1);
                    stock.set("insert_date",new Date());
                }

                if(StringUtils.isNotBlank(fc.getString("nickname"))){
                    stock.set("name",fc.get("nickname")+"-仓库");
                }else{
                    stock.set("name",fc.get("name")+"-仓库");
                }
                stock.set("address", fc.get("address"));
                stock.set("address_point", fc.get("address_point"));
                stock.set("last_date",new Date());
                Boolean r = stock.saveOrUpdate();
                if(r){
                    fc.put("stock_userid",fencangUser.get("id"));
                    fc.put("result",true);
                }else{
                    fc.put("result",false);
                }
                successfencang.add(fc);
            }
        }
        data.put("result",true);
        data.put("data",successfencang);
        return data;
    }
    /**
     * 微信营销平台商品同步初始化到库存货物-》根据规格
     * @param data
     * @param paramJson
     * @return
     */
    /*初始化商品*/
    @Before(Tx.class)
    public JSONObject initProductData(Account account, JSONObject data, JSONObject paramJson){
        JSONArray success = new JSONArray();
        JSONArray productData = paramJson.getJSONArray("data");

        for(int i=0;i<productData.size();i++) {
            JSONObject  fc = (JSONObject)productData.get(i);

            JSONArray spec= fc.getJSONArray("spec");

            for(int j=0;j<spec.size();j++){
                JSONObject jsonObj = spec.getJSONObject(j);
                Product product = Product.dao.getProductByWxGoodsCodeAndGoodsSpec(fc.getString("code"),jsonObj.getString("specCode"));
                boolean result = false;
                if(product == null){
                    product = new Product();
                    product.set("aid", account.get("id"));
                    product.set("code", GenerateSerialNo.createSerialProductCode());
                    product.set("wx_goods_code", fc.get("code"));
                    product.set("specCode", jsonObj.get(("specCode")));
                    product.setDate("insert_date");
                    product.set("status", 1);
                }
                ProductCategory productCategory=ProductCategory.dao.findFirst("select * from yzwstock_product_category where wx_category_code =?",fc.get("categoryCode"));
                if(productCategory!=null){
                    Integer id=productCategory.get("id");
                    product.set("category_id",id);
                }else{
                    jsonObj.put("result",false);
                    jsonObj.put("msg","分类不存在");
                    jsonObj.put("code",fc.get("code"));
                    success.add(jsonObj);
                    continue;
                }
                product.set("name", fc.get(("name")));
                product.set("pinyin", PinyinHelper.getShortPinyin(product.getName()));
                product.set("spec", jsonObj.get("specName"));
                product.set("price", jsonObj.get("price"));
                product.set("status", 1);
                product.setDate("last_date");
                result = product.saveOrUpdate();
                jsonObj.put("result",result);
                jsonObj.put("code",fc.get("code"));
                success.add(jsonObj);
            }
        }
        data.put("result",true);
        data.put("data",success);
        return data;
    }
    /*初始化商品分类*/
    @Before(Tx.class)
    public JSONObject initCategoryData(Account account,JSONObject data,JSONObject paramJson){
        JSONArray success = new JSONArray();
        JSONArray productD=paramJson.getJSONArray("data");
        for(int i = 0; i<productD.size(); i++) {
            Map<String,Object> rs = new HashMap<String,Object>();
            JSONObject  fc = (JSONObject)productD.get(i);
            ProductCategory category = ProductCategory.dao.getCategoryByWxCategoryCode(fc.get("code"));
            if(category==null){
                category = new ProductCategory();
                category.set("aid", account.get("id"));
                category.set("wx_category_code", fc.get("code"));
                category.setDate("insert_date");
            }
            category.set("name", fc.get(("name")));
            category.setDate("last_date");
            boolean result = category.saveOrUpdate();
            if (result) {
                rs.put("result", true);
            } else {
                rs.put("result", false);
            }
            rs.put("result",result);
            rs.put("data",data);
            success.add(rs);
        }
        data.put("result",true);
        data.put("data",success);
        return data;
    }
    @Before(Tx.class)

    public synchronized JSONObject outStorageByPfOrder(Account account,JSONObject data,JSONObject paramJson){
        JSONArray success = new JSONArray();
        JSONObject orderData = paramJson.getJSONObject("data");
        if(orderData!=null){
            OutStorage oldOutStorage = OutStorage.dao.findFirst("select * from yzwstock_out_storage where order_code = ? ",orderData.get("orderCode"));
            if(oldOutStorage!=null){
                data.put("result", false);
                data.put("msg", "该订单已经进入了库存系统,请勿重复操作！");
                return data;
            }
        }
        //最优距离仓库（微信系统传入）
        int stock_id = orderData.getInteger("stockid");//
        Stock stock = Stock.dao.findById(stock_id);
        JSONArray detail = orderData.getJSONArray("detail");
        //检查是否满足出库：1、商品及商品规格是否存在。2、选中仓库中该商品数量是否满足出库数量
        int isEableCode = 0;
        for(int i=0;i<detail.size();i++) {
            JSONObject dt = (JSONObject) detail.get(i);
            Product pro = Product.dao.findFirst("select * from yzwstock_product where wx_goods_code = ? and specCode = ? and status = 1", dt.get("goodsCode"), dt.get("goodsSpec"));
            //商品及商品规格是否存在
            if (pro == null) {
                data.put("result", false);
                data.put("msg", "库存商品数据与微信商品数据不同步！");
                isEableCode = 1;
                break;
            }
            //选中仓库中该商品数量是否满足出库数量

            if(!StockProduct.hasFullOutNums(stock_id,pro.getInt("id"),dt.getInteger("nums"))){
                data.put("result", false);
                data.put("msg", "该仓库中没有足够商品数量满足出库，请重新选择仓库！");
                isEableCode = 2;
                break;
            }
        }

        if(isEableCode == 0){

            OutStorage outStorage = new OutStorage();
            outStorage.set("aid", account.get("id"));
            outStorage.set("code", GenerateSerialNo.createSerialOutStorageCode());//生成单号数据
            outStorage.set("order_code", orderData.get("orderCode"));
            outStorage.set("total_fee", orderData.get("total_fee"));
            outStorage.set("out_type", 4);//订单出库
            outStorage.setDate("insert_date");
            outStorage.setDate("last_date");
            outStorage.set("insert_user_id",stock.get("admin"));
            outStorage.set("status",1);//待审批确定

            OutStorageAddress outStorageAddress = new OutStorageAddress();
            JSONObject addressObj = orderData.getJSONObject("address");
            if(addressObj!=null){
                outStorageAddress.set("name",addressObj.get("personName"));
                outStorageAddress.set("telephone",addressObj.get("telephone"));
                outStorageAddress.set("loc_province",addressObj.get("loc_province"));
                outStorageAddress.set("loc_city",addressObj.get("loc_city"));
                outStorageAddress.set("loc_town",addressObj.get("loc_town"));
                outStorageAddress.set("detail_address",addressObj.get("detail_address"));
                outStorageAddress.set("loc_province_code",addressObj.get("loc_province_code"));
                outStorageAddress.set("loc_city_code",addressObj.get("loc_city_code"));
                outStorageAddress.set("loc_town_code",addressObj.get("loc_town_code"));
                outStorageAddress.set("insert_date",new Date());
                outStorageAddress.set("last_date",new Date());
                outStorageAddress.save();
                outStorage.set("out_address_id",outStorageAddress.get("id"));
                JSONObject customerObj = orderData.getJSONObject("customer");
                if(customerObj!=null){
                    Customer customer = Customer.dao.findFirst("select * from yzwstock_customer where openid = ?",customerObj.get("openid"));
                    if(customer==null){
                        customer = new Customer();
                        customer.set("code", GenerateSerialNo.createSerialSupplierCode());
                        customer.set("insert_date",new Date());
                        customer.set("aid",account.get("id"));
                    }
                    customer.set("name",addressObj.get("personName"));
                    customer.set("contact",addressObj.get("personName"));
                    customer.set("address",addressObj.get("loc_province")+" "+addressObj.get("loc_city")+" "+addressObj.get("loc_town")+" "+addressObj.get("detail_address"));
                    customer.set("openid",customerObj.get("openid"));
                    customer.set("last_date",new Date());
                    customer.set("status",1);
                    customer.saveOrUpdate();
                }
            }

            boolean result = outStorage.save();

            for(int i=0;i<detail.size();i++){
                JSONObject dt= (JSONObject)detail.get(i);
                OutStorageDetail outStorageDetail = new OutStorageDetail();
                outStorageDetail.set("outstorage_id",outStorage.get("id"));
                Product pro = Product.dao.findFirst("select * from yzwstock_product where wx_goods_code = ? and specCode = ? and status = 1",dt.get("goodsCode"),dt.get("goodsSpec"));
                if(pro!=null){
                    outStorageDetail.set("product_id", pro.get("id"));
                }else{
                    data.put("result", false);
                    data.put("msg","库存商品数据与微信商品数据不同步！");

                }
                outStorageDetail.set("product_price", dt.get("price"));
                outStorageDetail.set("product_num", dt.get("nums"));
                outStorageDetail.set("stock_id", stock_id);
                result = outStorageDetail.save();
                if(result){
                    dt.put("result",true);
                }else{
                    dt.put("result", false);
                }
                dt.put("stocknums",StockProduct.getProdctStockNumsWithExcludelocking(stock_id,pro.getInt("id")));
                success.add(dt);
            }

            data.put("orderCode", orderData.get("orderCode"));
            data.put("status", 10);
            data.put("result", true);
            data.put("data",success);
            data.put("isEableCode", isEableCode);
        }else{
            data.put("orderCode", orderData.get("orderCode"));
            data.put("status", 10);
            data.put("result", false);
            data.put("isEableCode", isEableCode);
        }

        return data;
    }

    public JSONObject outStorageByWXPfOrder(Account account, JSONObject data, JSONObject paramJson) {
        JSONArray success = new JSONArray();
        JSONObject orderData = paramJson.getJSONObject("data");
        if(orderData!=null){
            OutStorage oldOutStorage = OutStorage.dao.findFirst("select * from yzwstock_out_storage where order_code = ? ",orderData.get("orderCode"));
            if(oldOutStorage!=null){
                data.put("result", false);
                data.put("msg", "该订单已经进入了库存系统,请勿重复操作！");
                //最优距离仓库（微信系统传入）
                int stock_id = orderData.getInteger("stockid");//
                Stock stock = Stock.dao.findById(stock_id);
                JSONArray detail = orderData.getJSONArray("detail");
                //检查是否满足出库：1、商品及商品规格是否存在。2、选中仓库中该商品数量是否满足出库数量
                int isEableCode = 0;
                for(int i=0;i<detail.size();i++) {
                    JSONObject dt = (JSONObject) detail.get(i);
                    Product pro = Product.dao.findFirst("select * from yzwstock_product where wx_goods_code = ? and specCode = ? and status = 1", dt.get("goodsCode"), dt.get("goodsSpec"));
                    //商品及商品规格是否存在
                    if (pro == null) {
                        data.put("result", false);
                        data.put("msg", "库存商品数据与微信商品数据不同步！");
                        isEableCode = 1;
                        break;
                    }
                    //选中仓库中该商品数量是否满足出库数量

                    if(!StockProduct.hasFullOutNums(stock_id,pro.getInt("id"),dt.getInteger("nums"))){
                        data.put("result", false);
                        data.put("msg", "该仓库中没有足够商品数量满足出库，请重新选择仓库！");
                        isEableCode = 2;
                        break;
                    }
                }

                if(isEableCode == 0) {
                    OutStorage outStorage = new OutStorage();
                    outStorage.set("aid", account.get("id"));
                    outStorage.set("code", GenerateSerialNo.createSerialOutStorageCode());//生成单号数据
                    outStorage.set("order_code", orderData.get("orderCode"));
                    outStorage.set("total_fee", orderData.get("total_fee"));
                    outStorage.set("out_type", 4);//订单出库
                    outStorage.setDate("insert_date");
                    outStorage.setDate("last_date");
                    outStorage.set("insert_user_id", stock.get("admin"));
                    outStorage.set("status", 0);//初始生成-未完成
                    outStorage.set("out_type", 5);

                    OutStorageAddress outStorageAddress = new OutStorageAddress();
                    JSONObject addressObj = orderData.getJSONObject("address");
                    if(addressObj!=null){
                        outStorageAddress.set("name",addressObj.get("personName"));
                        outStorageAddress.set("telephone",addressObj.get("telephone"));
                        outStorageAddress.set("loc_province",addressObj.get("loc_province"));
                        outStorageAddress.set("loc_city",addressObj.get("loc_city"));
                        outStorageAddress.set("loc_town",addressObj.get("loc_town"));
                        outStorageAddress.set("detail_address",addressObj.get("detail_address"));
                        outStorageAddress.set("loc_province_code",addressObj.get("loc_province_code"));
                        outStorageAddress.set("loc_city_code",addressObj.get("loc_city_code"));
                        outStorageAddress.set("loc_town_code",addressObj.get("loc_town_code"));
                        outStorageAddress.set("insert_date",new Date());
                        outStorageAddress.set("last_date",new Date());
                        outStorageAddress.save();
                        outStorage.set("out_address_id",outStorageAddress.get("id"));

                        JSONObject customerObj = orderData.getJSONObject("customer");
                        if(customerObj!=null){
                            Customer customer = Customer.dao.findFirst("select * from yzwstock_customer where openid = ?",customerObj.get("openid"));
                            if(customer==null){
                                customer = new Customer();
                                customer.set("code", GenerateSerialNo.createSerialSupplierCode());
                                customer.set("insert_date",new Date());
                                customer.set("aid",account.get("id"));
                            }
                            customer.set("name",addressObj.get("personName"));
                            customer.set("contact",addressObj.get("personName"));
                            customer.set("address",addressObj.get("loc_province")+" "+addressObj.get("loc_city")+" "+addressObj.get("loc_town")+" "+addressObj.get("detail_address"));
                            customer.set("openid",customerObj.get("openid"));
                            customer.set("last_date",new Date());
                            customer.set("status",1);
                            customer.saveOrUpdate();
                        }
                    }



                    boolean result = outStorage.save();
                    for (int i = 0; i < detail.size(); i++) {
                        JSONObject dt = (JSONObject) detail.get(i);
                        OutStorageDetail outStorageDetail = new OutStorageDetail();
                        outStorageDetail.set("outstorage_id", outStorage.get("id"));
                        Product pro = Product.dao.findFirst("select * from yzwstock_product where wx_goods_code = ? and specCode = ? and status = 1", dt.get("goodsCode"), dt.get("goodsSpec"));
                        if (pro != null) {
                            outStorageDetail.set("product_id", pro.get("id"));
                        } else {
                            data.put("result", false);
                            data.put("msg", "库存商品数据与微信商品数据不同步！");

                        }
                        outStorageDetail.set("product_price", dt.get("price"));
                        outStorageDetail.set("product_num", dt.get("nums"));
                        outStorageDetail.set("stock_id", stock_id);
                        result = outStorageDetail.save();
                        if(orderData.getBoolean("nowoutstock")!=null&&orderData.getBoolean("nowoutstock")){
                            //随机出库（无须扫码出库）
                            List<StockProduct> products = StockProduct.dao.find("select  * from yzwstock_stock_product where stock_id =? and product_id = ? and status = 1", stock_id, outStorageDetail.get("product_id"));
                            int counts = products.size();
                            int product_num = outStorageDetail.get("product_num");
                            for (int n = 0; n < product_num; n++) {
                                Random random = new Random();
                                int random_num = random.nextInt(counts-n);
                                products.get(random_num).set("outstorage_id",outStorage.getId()).set("status", 3).saveOrUpdate();
                                products.remove(random_num);
                            }
                            outStorage.set("check",1);
                            outStorage.set("review",1);
                            outStorage.set("review_text",null);
                            outStorage.set("review_date", new Date());
                            outStorage.set("status", 6).set("out_type", 5).saveOrUpdate();//出库完成
                        }else{
                            outStorage.set("status", 0).set("out_type", 5).saveOrUpdate();//未支付
                        }
                        if (result) {
                            dt.put("result", true);
                        } else {
                            dt.put("result", false);
                        }
                        dt.put("stocknums", StockProduct.getProdctStockNumsWithExcludelocking(stock_id, pro.getInt("id")));
                        success.add(dt);
                    }

                    data.put("orderCode", orderData.get("orderCode"));
                    data.put("status", 10);
                    data.put("result", true);
                    data.put("data",success);
                    data.put("isEableCode", isEableCode);
                }else{
                    data.put("orderCode", orderData.get("orderCode"));
                    data.put("status", 10);
                    data.put("result", false);
                    data.put("isEableCode", isEableCode);
                }
            }
        }

        return  data;
    }

    @Before(Tx.class)
    public JSONObject orderOutSubmit(JSONObject data, JSONObject paramJson) {
        JSONArray orderData = paramJson.getJSONArray("data");
        JSONArray array = new JSONArray();
        if(orderData==null){
            return data;
        }
        for(int k=0;k<orderData.size();k++){
            JSONObject orderItem = orderData.getJSONObject(k);
            OutStorage outStorage = OutStorage.dao.findFirst("select * from yzwstock_out_storage where order_code = ? and out_type = 5 and status = 0", orderItem.get("orderCode"));
            if(outStorage!=null){
                List<OutStorageDetail> details = OutStorageDetail.dao.find("select * from yzwstock_out_storage_detail where outstorage_id = ?", outStorage.getId());
                for (int i = 0; i < details.size(); i++) {
                    OutStorageDetail outStorageDetail = details.get(i);
                    //随机出库（无须扫码出库）
                    List<StockProduct> products = StockProduct.dao.find("select  * from yzwstock_stock_product where stock_id =? and product_id = ? and status = 1", outStorageDetail.get("stock_id"), outStorageDetail.get("product_id"));
                    int counts = products.size();
                    int product_num = outStorageDetail.get("product_num");
                    //FIXME 刘英俊 修复 ConcurrentModificationException异常 问题 ,注：在迭代集合的过程中删除操作需使用list.iterator()迭代
                    for (int n = 0; n < product_num; n++) {
                        Random random = new Random();
                        int random_num = random.nextInt(counts-n);
                        products.get(random_num).set("outstorage_id",outStorage.getId()).set("status", 3).saveOrUpdate();
                        products.remove(random_num);
                    }
                }
                outStorage.set("check",1);
                outStorage.set("review",1);
                outStorage.set("review_text",null);
                outStorage.set("review_date", new Date());
                outStorage.set("status", 6).set("out_type", 5).saveOrUpdate();//出库完成
                orderItem.put("orderCode", orderItem.get("orderCode"));
                orderItem.put("result", true);
            }else{
                outStorage = OutStorage.dao.findFirst("select * from yzwstock_out_storage where order_code = ? and out_type = 5 and status = 6", orderItem.get("orderCode"));
                if(outStorage!=null){
                    orderItem.put("orderCode", orderItem.get("orderCode"));
                    orderItem.put("result", true);
                    orderItem.put("msg", "已经出库");
                }else{
                    orderItem.put("orderCode", orderItem.get("orderCode"));
                    orderItem.put("result", false);
                    orderItem.put("msg", "出库单不存在，或已经出库");
                }

            }
            array.add(orderItem);
        }
        data.put("result",true);
        data.put("data",array);
        return  data;
    }

    @Before(Tx.class)
    public JSONObject getOrderOutStatus(Account account,JSONObject data,JSONObject paramJson){

        JSONObject orderStatus = paramJson.getJSONObject("data");
        String orderCode = orderStatus.getString("orderCode");
        OutStorage outStorage = OutStorage.dao.findFirst("select * from yzwstock_out_storage where order_code=?", orderCode);
        data.put("orderCode", orderStatus.getString("orderCode"));
        if(outStorage != null){
            Integer status = outStorage.get("status");
            data.put("status", status);
            if(status!=null){
                data.put("result", true);
                switch (status){
                    case 1:
                        data.put("statusName", "准备出库");
                        break;
                    case 2:
                        data.put("statusName", "仓库已审核");
                        break;
                    case 3:
                        data.put("statusName", "仓库审批不通过");
                        break;
                    case 5:
                        data.put("statusName", "正在拣货打包中");
                        break;
                    case 6:
                        data.put("statusName", "出库成功");
                        break;
                    default:
                        data.put("result", false);
                        data.put("statusName", "系统出库单状态异常");
                        break;
                }
            }else{
                data.put("result", false);
                data.put("statusName", "系统出库单状态异常");
            }

        }else {
            data.put("msg","状态返回失败！");
            data.put("result", false);
        }
        return data;
    }

    /**
     * 批量获取出库单状态
     * @param data
     * @param paramJson
     * @return
     */
    public JSONObject batchGetOutOrderStatus(JSONObject data,JSONObject paramJson){
        JSONArray orderData = paramJson.getJSONArray("data");
        JSONArray array = new JSONArray();
        if(orderData==null){
            return data;
        }
        for(int i=0;i<orderData.size();i++){
            JSONObject dataItem = orderData.getJSONObject(i);
            String orderCode = dataItem.getString("orderCode");
            OutStorage outStorage = OutStorage.dao.findFirst("select * from yzwstock_out_storage where order_code=?", orderCode);
            if(outStorage != null){
                Integer status = outStorage.get("status");
                dataItem.put("status", status);
                if(status!=null){
                    dataItem.put("result", true);
                    switch (status){
                        case 1:
                            dataItem.put("statusName", "准备出库");
                            break;
                        case 2:
                            dataItem.put("statusName", "仓库已审核");
                            break;
                        case 3:
                            dataItem.put("statusName", "仓库验对通过");
                            break;
                        case 5:
                            dataItem.put("statusName", "正在拣货打包中");
                            break;
                        case 6:
                            dataItem.put("statusName", "出库成功");
                            break;
                        default:
                            dataItem.put("result", false);
                            dataItem.put("statusName", "系统出库单状态异常");
                            break;
                    }
                }else{
                    dataItem.put("result", false);
                    dataItem.put("statusName", "系统出库单状态异常");
                }

            }else {
                dataItem.put("msg","状态返回失败！");
                dataItem.put("result", false);
            }
            array.add(dataItem);
        }
        data.put("result",true);
        data.put("data",array);
        return data;
    }

    @Before(Tx.class)
    public JSONObject synGoodsStockNums(Account account,JSONObject data,JSONObject paramJson){
        JSONArray success = new JSONArray();
        JSONArray productData = paramJson.getJSONArray("data");

        for(int i=0;i<productData.size();i++) {
            JSONObject  fc = (JSONObject)productData.get(i);

            JSONArray spec= fc.getJSONArray("spec");

            for(int j=0;j<spec.size();j++){
                JSONObject jsonObj = spec.getJSONObject(j);
                Product product = Product.dao.getProductByWxGoodsCodeAndGoodsSpec(fc.getString("code"),jsonObj.getString("specCode"));
                boolean result = false;
                if(product == null){
                    product = new Product();
                    product.set("aid", account.get("id"));
                    product.set("code", GenerateSerialNo.createSerialProductCode());
                    product.set("wx_goods_code", fc.get("code"));
                    product.set("specCode", jsonObj.get(("specCode")));
                    product.setDate("insert_date");
                    product.set("status", 1);
                }
                ProductCategory productCategory=ProductCategory.dao.findFirst("select * from yzwstock_product_category where wx_category_code =?",fc.get("categoryCode"));
                if(productCategory!=null){
                    Integer id=productCategory.get("id");
                    product.set("category_id",id);
                }else{
                    jsonObj.put("result",false);
                    jsonObj.put("msg","分类不存在");
                    jsonObj.put("code",fc.get("code"));
                    success.add(jsonObj);
                    continue;
                }
                product.set("name", fc.get(("name")));
                product.set("pinyin", PinyinHelper.getShortPinyin(product.getName()));
                product.set("spec", jsonObj.get("specName"));
                product.set("price", jsonObj.get("price"));
                product.set("status", 1);
                product.setDate("last_date");
                result = product.saveOrUpdate();
                jsonObj.put("result",result);
                jsonObj.put("code",fc.get("code"));
                //FIXME 刘英俊 2015-09-17
                //jsonObj.put("stocknums",StockProduct.getProdctStockNumsWithExcludelocking(0, product.getId()));//
                JSONArray array = new JSONArray();
                List<Stock> stockList = Stock.dao.find("select * from yzwstock_stock where aid = ?",account.getId());
                for(Stock stock:stockList){
                    JSONObject o = new JSONObject();
                    o.put("stockid",stock.getId());
                    o.put("stocknums",StockProduct.getProdctStockNumsWithExcludelocking(stock.getId(), product.getId()));//
                    array.add(o);
                }
                jsonObj.put("stockData",array);
                success.add(jsonObj);
            }
        }
        data.put("result",true);
        data.put("data",success);
        return data;
    }

    /**
     * 同步正在关注的微信粉丝
     * @param account
     * @param data
     * @param paramJson
     * @return
     */
    public JSONObject syncWxbVIP(Account account,JSONObject data,JSONObject paramJson){
        JSONArray success = new JSONArray();
        JSONArray productData = paramJson.getJSONArray("data");
        System.out.println(productData.size());
        /**
         * 判断是否有重复数据同步
         */
        Customer customerList =  null;
        Customer customer=null;
        Customer customer_first=Customer.dao.findFirst("select * from yzwstock_customer where aid=?",account.get("id"));
        //首次同步微信会员
        if(customer_first==null){
            for(int i=0;i<productData.size();i++) {
                JSONObject fc = (JSONObject) productData.get(i);
                customer = new Customer();
                customer.set("aid", account.get("id"));
                customer.set("code", GenerateSerialNo.createSerialSupplierCode());
                customer.setDate("insert_date");
                customer.setDate("last_date");
                customer.set("name", fc.get("name"));
                customer.set("address", fc.get("address"));
                customer.set("openid", fc.get("openid"));
                customer.set("status",1);
                customer.set("type",0);
                customer.save();
            }
        }else{
            for(int i=0;i<productData.size();i++) {
                JSONObject fc = (JSONObject) productData.get(i);
                customer = new Customer();
                customerList=Customer.dao.findFirst("SELECT * FROM yzwstock_customer where aid=? and openid=?",fc.get("openid"), account.get("id"));
                if(customerList!=null){
                    customer.set("aid", account.get("id"));
                    customer.set("code", GenerateSerialNo.createSerialSupplierCode());
                    customer.setDate("insert_date");
                    customer.setDate("last_date");
                    customer.set("name", fc.get("name"));
                    customer.set("address", fc.get("address"));
                    customer.set("status",1);
                    customer.set("type",0);
                    //FIXME 柯真 2015-10-20
                    customer.save();
                }
            }
        }
        data.put("result",true);
        data.put("data",success);
        return data;
    }

    @Before(Tx.class)
    public JSONObject getAllStockData(Account account,JSONObject data,JSONObject paramJson){
        List<Stock> stockList = Stock.dao.find("select * from yzwstock_stock where aid = ? and status = 1",account.get("id"));
        JSONArray array = new JSONArray();
        for(Stock stock:stockList){
            JSONObject o = new JSONObject();
            o.put("stockid",stock.get("id"));
            o.put("stockname",stock.get("name"));
            o.put("address",stock.get("address"));
            o.put("addresspoint",stock.get("address_point"));
            o.put("userid", stock.get("admin"));
            array.add(o);
        }
        data.put("result",true);
        data.put("data",array);
        return data;
    }

    @Before(Tx.class)
    public JSONObject getAllStockDataByOrder(Account account,JSONObject data,JSONObject paramJson){
        List<Stock> stockList = Stock.dao.find("select * from yzwstock_stock where aid = ? and status = 1",account.get("id"));
        JSONArray array = new JSONArray();
        for(Stock stock:stockList){
            JSONObject o = new JSONObject();
            o.put("stockid",stock.get("id"));
            o.put("stockname",stock.get("name"));
            o.put("address",stock.get("address"));
            o.put("addresspoint",stock.get("address_point"));
            o.put("userid",stock.get("admin"));

            //判断是否可以满足订单出库

            //检查是否满足出库：1、商品及商品规格是否存在。2、选中仓库中该商品数量是否满足出库数量
            JSONObject orderData = paramJson.getJSONObject("data");
            //最优距离仓库（微信系统传入）
            JSONArray detail = orderData.getJSONArray("detail");
            int isEableCode = 0;
            for(int i=0;i<detail.size();i++) {
                JSONObject dt = (JSONObject) detail.get(i);
                Product pro = Product.dao.findFirst("select * from yzwstock_product where wx_goods_code = ? and specCode = ? and status = 1", dt.get("goodsCode"), dt.get("goodsSpec"));
                //商品及商品规格是否存在
                if (pro == null) {
                    o.put("result", false);
                    o.put("msg", "库存商品数据与微信商品数据不同步！");
                    isEableCode = 1;
                    continue;
                }
                //选中仓库中该商品数量是否满足出库数量

                if(!StockProduct.hasFullOutNums(stock.getInt("id"),pro.getInt("id"),dt.getInteger("nums"))){
                    o.put("result", false);
                    o.put("msg", "该仓库中没有足够商品数量满足出库，请重新选择仓库！");
                    isEableCode = 2;
                    continue;
                }
            }
            o.put("isEableCode", isEableCode);
            array.add(o);
        }
        data.put("result",true);
        data.put("data",array);
        return data;
    }

    @Before(Tx.class)
    public JSONObject getEnAbleOutStockDataByOrder(Account account,JSONObject data,JSONObject paramJson){
        List<Stock> stockList = Stock.dao.find("select * from yzwstock_stock where aid = ? and status = 1",account.get("id"));
        JSONArray array = new JSONArray();
        for(Stock stock:stockList){
            JSONObject o = new JSONObject();
            o.put("stockid",stock.get("id"));
            o.put("stockname",stock.get("name"));
            o.put("address",stock.get("address"));
            o.put("addresspoint",stock.get("address_point"));
            o.put("userid",stock.get("admin"));

            //判断是否可以满足订单出库

            //检查是否满足出库：1、商品及商品规格是否存在。2、选中仓库中该商品数量是否满足出库数量
            JSONObject orderData = paramJson.getJSONObject("data");
            //最优距离仓库（微信系统传入）
            JSONArray detail = orderData.getJSONArray("detail");
            int isEableCode = 0;
            for(int i=0;i<detail.size();i++) {
                JSONObject dt = (JSONObject) detail.get(i);
                Product pro = Product.dao.findFirst("select * from yzwstock_product where wx_goods_code = ? and specCode = ? and status = 1", dt.get("goodsCode"), dt.get("goodsSpec"));
                //商品及商品规格是否存在
                if (pro == null) {
                    o.put("result", false);
                    o.put("msg", "库存商品数据与微信商品数据不同步！");
                    isEableCode = 1;
                    continue;
                }
                //选中仓库中该商品数量是否满足出库数量

                if(!StockProduct.hasFullOutNums(stock.getInt("id"),pro.getInt("id"),dt.getInteger("nums"))){
                    o.put("result", false);
                    o.put("msg", "该仓库中没有足够商品数量满足出库，请重新选择仓库！");
                    isEableCode = 2;
                    continue;
                }
            }
            if(isEableCode==0){
                o.put("isEableCode", isEableCode);
                array.add(o);
            }

        }
        data.put("result",true);
        data.put("data",array);
        return data;
    }

    public JSONObject outStorageList(JSONObject data,JSONObject paramJson){
        //统计出库详细
        String out_detail_sql = "select a.id,a.outstorage_id,a.product_num,c.id as product_id,c.name as product_name,c.spec,d.name as stock_name,d.id as stock_id\n" +
                "from yzwstock_out_storage_detail a \n" +
                "left join yzwstock_out_storage b on a.outstorage_id = b.id\n" +
                "left join yzwstock_product c on a.product_id = c.id\n" +
                "left join yzwstock_stock d on a.stock_id = d.id\n" +
                "where b.code = ?";

        //统计已出库货品数量
        String has_out_sql = "select count(*) as has_out from yzwstock_stock_product where stock_id = ? and product_id = ? and outstorage_id = ?";

        List<OutStorageDetail> detailList = OutStorageDetail.dao.find(out_detail_sql,paramJson.get("out_code"));
        JSONArray array = new JSONArray();

        for(OutStorageDetail detail : detailList){
            StockProduct sp = StockProduct.dao.findFirst(has_out_sql,detail.get("stock_id"),detail.get("product_id"),detail.get("outstorage_id"));
            JSONObject jo = new JSONObject();

            Integer count = detail.getInt("product_num");
            Integer has_out = sp.getLong("has_out").intValue();
            Integer not_out = count - has_out;

            jo.put("outstorage_id",detail.get("outstorage_id"));
            jo.put("product_name",detail.get("product_name"));
            jo.put("product_num", detail.get("product_num"));
            jo.put("has_out",has_out);
            jo.put("not_out",not_out);
            jo.put("spec", detail.get("spec"));

            array.add(jo);
        }

        //判断出库单是否完成出库
        Integer out_status = Db.queryInt("select status from yzwstock_out_storage where code = ?", paramJson.get("out_code"));

        if(out_status == 6){
            data.put("result",false);
            data.put("object",array);
            data.put("msg","该出库单已经完成出库");
        }else if(out_status < 3){
            data.put("result",false);
            data.put("msg","该出库单暂时不可出库");
        }else{
            if(detailList == null){
                data.put("msg","没有库存!");
                data.put("result",false);
            }else{
                data.put("object",array);
                data.put("result",true);
            }
        }

        return data;
    }

    public JSONObject outStorageDetail(JSONObject data,JSONObject paramJson){
        StockProduct sp_obj = StockProduct.dao.findFirst("select product_id,stock_id,status from yzwstock_stock_product where code = ?", paramJson.get("sp_code"));
        List<OutStorageDetail> detail_list = OutStorageDetail.dao.find("select distinct a.product_id,a.stock_id from yzwstock_out_storage_detail a left join yzwstock_out_storage b on b.id = a.outstorage_id where b.code = ?", paramJson.get("out_code"));
        boolean flag1 = true;
        boolean flag2 = true;
        boolean flag3 = false;

        //判断该货品是否已被扫码出库
        if(sp_obj.getInt("status") == 2){
            data.put("msg","该货品已被扫码,请选择其他货品");
            data.put("result",false);
            return data;
        }else if(sp_obj.getInt("status") == 3){
            data.put("msg","该货品已出库,请选择其他货品");
            data.put("result",false);
            return data;
        }else if(sp_obj.getInt("status") == 0){
            data.put("msg","该货品已被删除,请选择其他货品");
            data.put("result",false);
            return data;
        }

        //判断出库单是否包含该货品
        for(OutStorageDetail detail1 : detail_list){
            if(sp_obj.getInt("product_id").intValue() != detail1.getInt("product_id").intValue()){
                flag1 = false;
            }else{
                flag1 = true;
                break;
            }
        }

        if(!flag1){
            data.put("result",false);
            data.put("msg","该货品不在出库单中");
            return data;
        }

        //判断出库单是否包含该仓库
        for(OutStorageDetail detail2 : detail_list){
            if(sp_obj.getInt("stock_id").intValue() != detail2.getInt("stock_id").intValue()){
                flag2 = false;
            }else{
                flag2 = true;
                break;
            }
        }

        if(!flag2){
            data.put("result",false);
            data.put("msg","该出库单不包含此仓库的货品");
            return data;
        }

        //判断出库单是否包含该仓库的该货品
        for(OutStorageDetail detail3 : detail_list){
            if((sp_obj.getInt("stock_id") == detail3.getInt("stock_id").intValue()) && (sp_obj.getInt("product_id") == detail3.getInt("product_id").intValue())){
                flag3 = true;
                break;
            }else{
                flag3 = false;
            }
        }

        if(!flag3){
            data.put("result",false);
            data.put("msg","该出库单不包含此仓库的此货品");
            return data;
        }

        //判断对应仓库的对应货品是否出齐
        //Fixme  徐晓辉 2015-09-15
        List<OutStorageDetail> detailList = OutStorageDetail.dao.find("select a.id,a.outstorage_id,a.product_id,a.stock_id from yzwstock_out_storage_detail a left join yzwstock_out_storage b on a.outstorage_id = b.id where b.code = ?",paramJson.get("out_code"));
        StockProduct stockProduct = StockProduct.dao.findFirst("select product_id,stock_id from yzwstock_stock_product where code = ?", paramJson.get("sp_code"));
        OutStorageDetail out_detail = OutStorageDetail.dao.findFirst("select IFNULL(a.product_num,0) as out_product_num,a.outstorage_id from yzwstock_out_storage_detail a left join yzwstock_out_storage b on b.id = a.outstorage_id  where b.code = ? and a.stock_id = ? and a.product_id = ?", paramJson.get("out_code"), stockProduct.get("stock_id"),stockProduct.get("product_id"));

        Long out_count = Db.queryLong("select IFNULL(count(*),0) from yzwstock_stock_product a where a.stock_id = ? and a.product_id = ? and a.outstorage_id = ?", stockProduct.get("stock_id"), stockProduct.get("product_id"), out_detail.get("outstorage_id"));
        Integer product_count = out_detail.getLong("out_product_num").intValue();

        if(out_count.intValue() == product_count){
            data.put("result",false);
            data.put("msg","出库单中此仓库的此货品已经出完，请选择其他的货品");
            return data;
        }

        String detail_sql = "select a.code,a.insert_date,b.name as product_name,b.spec\n" +
                "from yzwstock_stock_product a\n" +
                "left join yzwstock_product b on a.product_id = b.id\n" +
                "where a.code = ?";
        StockProduct sp = StockProduct.dao.findFirst(detail_sql,paramJson.get("sp_code"));

        data.put("product_name",sp.get("product_name"));
        data.put("spec",sp.get("spec"));
        data.put("sp_code",sp.get("code"));
        data.put("insert_date",sp.get("insert_date"));
        data.put("result",true);

        return data;
    }

    public JSONObject submitOutStorage(JSONObject data,JSONObject paramJson){
        //FIXME  刘英俊 2015-09-13
        //OutStorage os = OutStorage.dao.findFirst("select a.id,b.product_num from yzwstock_out_storage a left join yzwstock_out_storage_detail b on a.id = b.outstorage_id where a.code = ?",paramJson.get("out_code"));
        OutStorage os = OutStorage.dao.findFirst("select * from yzwstock_out_storage a where a.code = ?",paramJson.get("out_code"));
        if(os!=null&&os.getInt("status")< 6){
            Long sum_product_num = Db.queryBigDecimal("select IFNULL(sum(a.product_num),0)  from yzwstock_out_storage_detail a left join yzwstock_out_storage b on b.id = a.outstorage_id  where  b.code = ?", paramJson.get("out_code")).longValue();
            String update_sql = "update yzwstock_stock_product set outstorage_id = ?,out_time = ?,status = ? where code = ?";
            Integer i = Db.update(update_sql, os.get("id"), new Date(), 2, paramJson.get("sp_code"));
            Long count = Db.queryLong("select count(*) from yzwstock_stock_product a where a.outstorage_id = ?", os.get("id"));

            Integer status = 0;

            //FIXME  刘英俊 2015-09-13
            if(count.intValue() < sum_product_num){
                status = 5;
            }else{
                status = 6;
            }

            Db.update("update yzwstock_out_storage set status = ? where code = ?",status,paramJson.get("out_code"));
            //FIXME  刘英俊 2015-09-13
            //如果已经成功完成扫码（全部货物数量）
            if(status==6){
                Db.update("update yzwstock_stock_product set status = 3 where outstorage_id = ?", os.get("id"));
            }
            if(i == null){
                data.put("msg","出库失败!");
            }else{
                data.put("msg","出库成功!");
                data.put("result",true);
                data.put("status",status);
            }

        }else{
            data.put("msg","该出库单已全部完成出库，请不要再扫码货物!");
            data.put("errorCode",1011);
            data.put("result",false);
        }

        return  data;
    }

    public JSONObject updateGoods(JSONObject data,JSONObject paramJson){
        String sql_1 = "update yzwstock_product set specCode = null,wx_goods_code = null where wx_goods_code = ? and specCode in (";
        String sql_2 = "";

        StringBuffer strBuffer = new StringBuffer("");
        JSONArray codeArray = paramJson.getJSONArray("specCodeArray");
        for(int i = 0; i < codeArray.size(); i++){
            strBuffer.append("'");
            strBuffer.append(codeArray.getJSONObject(i).getString("specCode"));
            strBuffer.append("',");
        }

        sql_2 = strBuffer.toString().substring(0,strBuffer.toString().length() - 1) + ")";

        int i = Db.update(sql_1 + sql_2, paramJson.get("wx_goods_code"));

        if(i > 0){
            data.put("msg","更新成功");
            data.put("result",true);
        }else{
            data.put("msg","更新失败");
            data.put("result",false);
        }

        return data;
    }

    public JSONObject clearAccountsData(Account account,JSONObject data){
        try{
            //删除库存预警表数据
            List<Warning> list = Warning.dao.listByStockId(account.getInt("id"));
            for(Warning warning:list){
                warning.delete();
            }
            //删除入库详情表数据
            List<InStorageDetail> indetails = InStorageDetail.dao.listByAid(account.getInt("id"));
            for(InStorageDetail detail: indetails){
                detail.delete();
            }
            //删除出库详情表数据
            List<OutStorageDetail> outdetails = OutStorageDetail.dao.listByAid(account.getInt("id"));
            for(OutStorageDetail detail: outdetails){
                detail.delete();
            }
            //清空库存表数据
            Db.update("delete from yzwstock_stock_product where aid=?",account.get("id"));
            //清空报损表数据
            Db.update("delete from yzwstock_reportbad where aid=?",account.get("id"));
            //清空入库表数据
            Db.update("delete from yzwstock_in_storage where aid=?",account.get("id"));
            //清空出库表数据
            Db.update("delete from yzwstock_out_storage where aid=?",account.get("id"));
            //清空调库表数据
            Db.update("delete from yzwstock_movestore where aid=?",account.get("id"));
            //清空产品表数据
            Db.update("delete from yzwstock_product where aid=?",account.get("id"));
            //清空仓库表数据
            Db.update("delete from yzwstock_stock where aid=?",account.get("id"));
            //清空产品分类表数据
            Db.update("delete from yzwstock_product_category where aid=?",account.get("id"));
            //清空供应商表数据
            Db.update("delete from yzwstock_supplier where aid=?",account.get("id"));
            //清空客户表数据
            Db.update("delete from yzwstock_customer where aid=?",account.get("id"));
            //删除用户信息
            Db.update("delete from admin_account where id=?",account.get("id"));

            data.put("result", true);
            data.put("msg","清除用户信息成功");
            return data;
        }catch(Exception e){
            logger.error("清空用户数据出错：clearAccountsData@Erro");
        }
        data.put("result",false);
        data.put("msg","清除用户信息失败，请重试！");
        return data;
    }

    public JSONObject synDeleteOutStorage(JSONObject data,JSONObject paramJson){
        String sql_1 = "update yzwstock_out_storage set status = -1 where order_code in (";
        String sql_2 = "";
        StringBuffer buffer = new StringBuffer("");
        JSONArray array = paramJson.getJSONArray("array");

        for(int i = 0; i < array.size(); i++){
              buffer.append("'");
              buffer.append(array.getJSONObject(i).get("order_code"));
              buffer.append("',");
        }

        sql_2 = buffer.toString().substring(0,buffer.toString().length() - 1) + ")";

        int i = Db.update(sql_1 + sql_2);

        if(i > 0){
            data.put("msg","更新成功");
            data.put("result",true);
        }else{
            data.put("msg","更新失败");
            data.put("result",false);
        }

        return data;
    }

    public  JSONObject delStockAllData(JSONObject data,JSONObject paramJson){
        int aid= (int) paramJson.get("aid");//平台账号ID
        //stock_id   instorage_id
        List<Integer> instorage_ids=new ArrayList<>();
        List<Integer> stock_ids=new ArrayList<>();
        boolean flag=false;
        if(aid!=-1){
            List<InStorage> inStorageList=InStorage.dao.find("select * from yzwstock_in_storage where aid=?",aid);
            if(inStorageList!=null&&inStorageList.size()>0){
                for(int i=0;i<inStorageList.size();i++){
                    Integer instorage_id=inStorageList.get(i).getId();
                    instorage_ids.add(instorage_id);
                }
            }
            List<Stock> stockList=Stock.dao.find("select * from yzwstock_stock where aid=?",aid);
            if(stockList!=null&&stockList.size()>0){
                for(int i=0;i<stockList.size();i++){
                    Integer stock_id=stockList.get(i).getId();
                    stock_ids.add(stock_id);
                }
            }
            if(instorage_ids.size()>0){
                for(int i=0;i<instorage_ids.size();i++){
                    Db.update("delete from yzwstock_in_storage_detail where instorage_id=?",instorage_ids.get(i));
                    Db.update("delete from yzwstock_movestore where instorage_id=?",instorage_ids.get(i));
                }
                flag=true;
            }
            if(stock_ids.size()>0){
                for(int i=0;i<stock_ids.size();i++){
                    Db.update("delete from yzwstock_out_storage_detail where stock_id=?",stock_ids.get(i));
                    Db.update("delete from yzwstock_product_stock_worning where stockid=?",stock_ids.get(i));
                }
                flag=true;
            }
            List<String> list= Db.query("SELECT TABLE_NAME  from information_schema.columns where TABLE_SCHEMA='yzwstock' and column_name ='aid'");
            for(int i=0;i<list.size();i++){
                StringBuilder sb=new StringBuilder();
                sb.append("delete from ");
                sb.append(list.get(i));
                sb.append(" where aid='").append(aid).append("'");
                int a= Db.update(sb.toString());
                if(a>0){
                    flag=true;
                }else{
                    flag=false;
                }
            }
            int i=Db.update("delete from admin_account where id=?",aid);
            if(i>0){
                flag=true;
                data.put("result", flag);
            }else{
                flag=false;
            }
        }
        return  data;
    }
}
