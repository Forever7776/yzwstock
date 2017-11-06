package com.yzw.stock.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.log.Logger;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.stock.service.WeixinPlatformApiService;
import com.yzw.system.model.Account;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;

/**
 * 微信平台库存接口Api
 * Created by liuyj on 2015/8/24.
 */
@ControllerBind(controllerKey = "/pf/api",viewPath = "/page/test")
public class WeixinPlatformApiCtrl extends Controller {
    private static final Logger logger = Logger.getLogger(WeixinPlatformApiCtrl.class);
    public WeixinPlatformApiService service = new WeixinPlatformApiService(this);
    public void index(){
        JSONObject data = new JSONObject();
        data.put("result", false);
        try {
            String mcode = this.getPara("mcode");
            //根据mcode参数对应的方法名分发处理
            data.put("mcode",mcode);
            JSONObject paramJson = null;
            try {
                // 读取请求内容
                BufferedReader br = new BufferedReader(new InputStreamReader(this.getRequest().getInputStream()));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while((line = br.readLine())!=null){
                    sb.append(line);
                }

                String reqBody = sb.toString();
                //logger.info("#######请求参数：" + reqBody);
                String json = reqBody.trim();
                logger.info("请求参数：" + json);
                paramJson = JSONObject.parseObject(json);
            }catch (Exception e){
                e.printStackTrace();
                logger.info("error：index@WeixinPlatformApiCtrl->请求数据中JSON数据解析错误！");
            }
            if(paramJson!=null&&StringUtils.isNotBlank(mcode)){
                switch (mcode){
                    case "login":
                        data = service.login(data, paramJson);
                        break;
                    case "newAccount":
                        data = service.newAccount(data, paramJson);
                        break;
                    case "outStorageList":
                        data = service.outStorageList(data, paramJson);
                        break;
                    case "outStorageDetail":
                        data = service.outStorageDetail(data, paramJson);
                        break;
                    case "submitOutStorage":
                        data = service.submitOutStorage(data, paramJson);
                        break;
                    case "batchGetOutOrderStatus":
                        data = service.batchGetOutOrderStatus(data, paramJson);
                        break;
                    case "orderOutSubmit":
                        data = service.orderOutSubmit(data, paramJson);
                        break;
                    //商城删除商品后,去除库存商品状态后的(与商城同步)字样
                    case "updateGoods":
                        data = service.updateGoods(data, paramJson);
                        break;
                    //系统删除过期订单,同步删除库存系统的对应出库单
                    case "synDeleteOutStorage":
                        data = service.synDeleteOutStorage(data, paramJson);
                        break;
                    //wxb项目后台进行销毁账号，同步销毁库存系统数据
                    case "delStockData":
                        data = service.delStockAllData(data,paramJson);
                    default:
                        //授权后访问的api
                        String token  = paramJson.getString("token");
                        if(StringUtils.isNotBlank(token)&&service.check(token)){
                            //解码获得account
                            String accountName = service.tokenDecode(token);
                            Account account = Account.dao.findFirst("select * from admin_account where name = ? ", accountName);
                            if(account==null){
                                data.put("result",false);
                                data.put("errorCode",1003);//
                                data.put("msg", "请求失败，该账号不存在！");
                            }else{
                                logger.info("当前账号信息："+account.get("name"));
                                switch (mcode){
                                    case "initStocks":
                                        data = service.initStockData(account, data, paramJson);
                                        break;
                                    case "initGoods":
                                        data = service.initProductData(account, data, paramJson);
                                        break;
                                    case "initCategory":
                                        data = service.initCategoryData(account, data, paramJson);
                                        break;
                                    case "orderOut":
                                        data = service.outStorageByPfOrder(account, data, paramJson);
                                        break;
                                    case "orderOutWithRandom":
                                        data = service.outStorageByWXPfOrder(account, data, paramJson);
                                        break;
                                    case "getOutStatus":
                                        data = service.getOrderOutStatus(account, data, paramJson);
                                    case "getAllStockData":
                                        data = service.getAllStockData(account, data, paramJson);
                                        break;
                                    case "getAllStockDataByOrder":
                                        data = service.getAllStockDataByOrder(account, data, paramJson);
                                        break;
                                    case "getEnAbleOutStockDataByOrder":
                                        data = service.getEnAbleOutStockDataByOrder(account, data, paramJson);
                                        break;
                                    case "synGoodsStockNums":
                                        data = service.synGoodsStockNums(account, data, paramJson);
                                        break;
                                    case "clearAccountsData":
                                        data = service.clearAccountsData(account, data);
                                        break;
                                    case "syncWxbVIP":
                                        data = service.syncWxbVIP(account, data, paramJson);
                                        break;
                                    default:
                                }
                            }

                        }else{
                            data.put("result",false);
                            data.put("errorCode",1001);//改错误编号不可乱用
                            data.put("msg", "请求失败，暂时未授权或授权已经失效！");
                        }
                }

        } else {
            data.put("result", false);
            data.put("msg","请求失败，mcode参数不能为空！或数据格式有错误");
        }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("系统错误：WeixinPlatformApiCtrl&index");
            data.put("result", false);
            data.put("msg", "库存系统错误或异常");
        }
        logger.info("返回数据：" +data);
        this.renderJson(data.toJSONString());
    }

    public void init(){
        this.render("test.html");
    }

    public void apitest(){
        try {
            byte[] b = new byte[1024*4];
            String jsonstr = "{\"token\":\"MTQ0NDQ0MjU5NTUyNHxodWltaW5nbm9uZ3ll\",\"data\":[{\"spec\":[{\"specCode\":\"201508270000000050\",\"specName\":\"份\"}],\"name\":\"通心菜 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"06f72c4c36dd477ea2a7e55b29875724\"},{\"spec\":[{\"specCode\":\"201508270000000042\",\"specName\":\"包\"}],\"name\":\"齐绿丝瓜 500g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"0dff5075f06b48a5bb68528ff40ca8b2\"},{\"spec\":[{\"specCode\":\"201508200000000001\",\"specName\":\"盒\"}],\"name\":\"野生茶油 礼品装 1.8L*2只\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"0e14479d186c41d5abd36f8e13547076\"},{\"spec\":[{\"specCode\":\"201508260000000014\",\"specName\":\"只\"}],\"name\":\"黑土鸡  8个月以上黑土鸡\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"198fbc1eaf384460a36fc9945d6ab67a\"},{\"spec\":[{\"specCode\":\"201508270000000058\",\"specName\":\"份\"}],\"name\":\"紫茄 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"1ec377d17c704860946d3943b2d3701f\"},{\"spec\":[{\"specCode\":\"201508270000000048\",\"specName\":\"份\"}],\"name\":\"番薯叶 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"22a31106b1dc44bf8e19989462791170\"},{\"spec\":[{\"specCode\":\"201508260000000017\",\"specName\":\"带皮五花肉  / 300g\"},{\"specCode\":\"201509170000000029\",\"specName\":\"肋 排 / 500g\"},{\"specCode\":\"201509170000000031\",\"specName\":\"多种规格/18-78元\"},{\"specCode\":\"201509170000000026\",\"specName\":\"前腿肉 / 300g\"},{\"specCode\":\"201509170000000027\",\"specName\":\"后腿肉  / 300g\"},{\"specCode\":\"201509170000000030\",\"specName\":\"龙 骨 / 400g\"},{\"specCode\":\"201509170000000028\",\"specName\":\"筒子骨 / 个\"}],\"name\":\"黑土猪 \",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"2ad8bde0b3d548d3b73126cd2b86cdff\"},{\"spec\":[{\"specCode\":\"201508270000000033\",\"specName\":\"盒\"}],\"name\":\"初生蛋 6枚\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"3066cca1ea714739ad79b54edbb99bb7\"},{\"spec\":[{\"specCode\":\"201508260000000015\",\"specName\":\"只\"}],\"name\":\"土鸭  8个月以上土鸭\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"3dbc562e44cb410096924efeae3a317e\"},{\"spec\":[{\"specCode\":\"201508270000000061\",\"specName\":\"盒\"}],\"name\":\"豆腐 400g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"3f4f70bbf1a54d46a308f9b55a022cc6\"},{\"spec\":[{\"specCode\":\"201508270000000059\",\"specName\":\"份\"}],\"name\":\"芥菜 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"3f61ac6f19aa47f2a0d8cd035891532c\"},{\"spec\":[{\"specCode\":\"201508260000000033\",\"specName\":\"个\"}],\"name\":\"香芋南瓜 1250-1500g\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"401a9e7c087445d7a651587deb90d662\"},{\"spec\":[{\"specCode\":\"201509080000000001\",\"specName\":\"经典款 4公4母\"},{\"specCode\":\"201509080000000002\",\"specName\":\"蟹王蟹后 4公4母\"}],\"name\":\"溱湖簖蟹 \",\"categoryCode\":\"815d31cb3cca4fc48d61cac536be0761\",\"code\":\"412a6576325c4baa950a10ae55dc48e9\"},{\"spec\":[{\"specCode\":\"201508270000000046\",\"specName\":\"份\"}],\"name\":\"生菜 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"43ea2db6ee8943c9864353ca8ac55c32\"},{\"spec\":[{\"specCode\":\"201508270000000041\",\"specName\":\"盒\"}],\"name\":\"玉米 2条\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"46bcfc2ee49d49269bcb658c4fbdd3c3\"},{\"spec\":[{\"specCode\":\"201508260000000021\",\"specName\":\"份\"}],\"name\":\"野生黑木耳 100g\",\"categoryCode\":\"815d31cb3cca4fc48d61cac536be0761\",\"code\":\"50b7c3c2425c41c8a82be43b0efac538\"},{\"spec\":[{\"specCode\":\"201508270000000032\",\"specName\":\"0.9-1.2KG/只\"}],\"name\":\"鸡 8个月放养（现宰）\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"55d49c4a77c94978b3ebf6a53d702861\"},{\"spec\":[{\"specCode\":\"201508260000000020\",\"specName\":\"包\"}],\"name\":\"野生松茸 烤干  100g\",\"categoryCode\":\"815d31cb3cca4fc48d61cac536be0761\",\"code\":\"5f63a6a9c0894ffcafdd9f630647703d\"},{\"spec\":[{\"specCode\":\"201508270000000051\",\"specName\":\"份\"}],\"name\":\"枸杞叶 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"6070a41e01124dbe813e34ffb886a492\"},{\"spec\":[{\"specCode\":\"201508190000000003\",\"specName\":\"盒\"}],\"name\":\"野生茶油 玻璃瓶 500ml*2只\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"60a07dd2cffc4b26bbb025270b94275c\"},{\"spec\":[{\"specCode\":\"201508270000000054\",\"specName\":\"份\"}],\"name\":\"秋葵 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"6c84f1ac75654cceab3a2997e253548c\"},{\"spec\":[{\"specCode\":\"201508200000000003\",\"specName\":\"只\"}],\"name\":\"土鸡 2年老母鸡 \",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"6da7d0c06e66489f832376efa0471e27\"},{\"spec\":[{\"specCode\":\"201508290000000002\",\"specName\":\"份\"}],\"name\":\"黄瓜 2条\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"726d13b7130d407389e88f0caac33d42\"},{\"spec\":[{\"specCode\":\"201508260000000018\",\"specName\":\"袋\"}],\"name\":\"五常大米 5kg\",\"categoryCode\":\"815d31cb3cca4fc48d61cac536be0761\",\"code\":\"7a5e2536d8ea44cb9b03ae6445c3786a\"},{\"spec\":[{\"specCode\":\"201508270000000047\",\"specName\":\"份\"}],\"name\":\"南瓜苗 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"7ba506160214492a83e5a521234cd36d\"},{\"spec\":[{\"specCode\":\"201509160000000005\",\"specName\":\"蓝莓巴菲\"},{\"specCode\":\"201509160000000008\",\"specName\":\"红豆杨枝甘露\"},{\"specCode\":\"201509160000000003\",\"specName\":\"火龙果巴菲\"},{\"specCode\":\"201509160000000004\",\"specName\":\"杏仁巴菲\"},{\"specCode\":\"201509160000000002\",\"specName\":\"芒果巴菲\"},{\"specCode\":\"201509160000000007\",\"specName\":\"西米杨枝甘露\"}],\"name\":\"手工酸奶\",\"categoryCode\":\"815d31cb3cca4fc48d61cac536be0761\",\"code\":\"82d8d2adb4e74d34a7d05ec68435a295\"},{\"spec\":[{\"specCode\":\"201508290000000004\",\"specName\":\"份\"}],\"name\":\"苦瓜 2条\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"841201a7b53642f48aea7d6bdd036824\"},{\"spec\":[{\"specCode\":\"201508260000000026\",\"specName\":\"份\"}],\"name\":\"红洋葱 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"85beff1dd0e74d7592d9b116373b938a\"},{\"spec\":[{\"specCode\":\"201508200000000002\",\"specName\":\"盒\"}],\"name\":\"有机湘莲 礼盒装 500g*2只\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"86f636a54d1743fd8b996a2288f77e21\"},{\"spec\":[{\"specCode\":\"201508190000000002\",\"specName\":\"盒\"}],\"name\":\"土鸡蛋  48枚\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"94c6096d0cf14466852e53febccd4434\"},{\"spec\":[{\"specCode\":\"201508290000000003\",\"specName\":\"份\"}],\"name\":\"长豆角 350g\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"94f4cb4467f44f9c89d4cf8665ceda7b\"},{\"spec\":[],\"name\":\"【众筹】藏香猪 散养革命老区井冈山脚下\",\"categoryCode\":\"3f89ddf043194b5b9ff454944ef06286\",\"code\":\"9a0b4644d79a4505b1b37f7132c5c748\"},{\"spec\":[{\"specCode\":\"201508260000000055\",\"specName\":\"包\"}],\"name\":\"手工牛筋丸 250g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"9d327d857c594390992b8163c29a4b08\"},{\"spec\":[{\"specCode\":\"201508260000000056\",\"specName\":\"份\"}],\"name\":\"手工牛肉丸 250g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"a1cf87e8170e49ffae68d6ed205918fd\"},{\"spec\":[{\"specCode\":\"201508270000000053\",\"specName\":\"包\"}],\"name\":\"青茄 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"a6fa417a301d4ea1acbf24d1b04d66c3\"},{\"spec\":[{\"specCode\":\"201508260000000016\",\"specName\":\"条\"}],\"name\":\"野生桂鱼 约500g-800g\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"ae4c536ba4cf4671b05b8025e2c920f6\"},{\"spec\":[{\"specCode\":\"201508270000000049\",\"specName\":\"份\"}],\"name\":\"小油麦 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"af9afc50d1c446b1a50baab760c6e1cc\"},{\"spec\":[{\"specCode\":\"201508250000000005\",\"specName\":\"3.5斤\"}],\"name\":\"生态甲鱼 3.5斤\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"ba906a7900114c1e9ada5e81c2ca1bd5\"},{\"spec\":[{\"specCode\":\"201509100000000006\",\"specName\":\"份\"}],\"name\":\"【赠品】“金秋六宝”蔬菜大礼包\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"c18fed42a9634e7ba910cf44b3206504\"},{\"spec\":[{\"specCode\":\"201508270000000057\",\"specName\":\"份\"}],\"name\":\"湖南椒 350g\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"d02d6a8427bd4242abaefbf834da5ea5\"},{\"spec\":[{\"specCode\":\"201508270000000060\",\"specName\":\"盒\"}],\"name\":\"酸豆角 350g\",\"categoryCode\":\"26ce4b5688db4bc1828bf196816dd6c7\",\"code\":\"d54e778127e048bb99099cee08fbbaf6\"},{\"spec\":[],\"name\":\"茭白\",\"categoryCode\":\"49ad7b528fd34c509b51b9d607cd1b59\",\"code\":\"e26dd2daf7ce42fe961ccf95d91cdcef\"},{\"spec\":[{\"specCode\":\"201509090000000009\",\"specName\":\"80斤左右/头\"}],\"name\":\"【伙拼】火箭猪 散养野猪 新年定制\",\"categoryCode\":\"815d31cb3cca4fc48d61cac536be0761\",\"code\":\"e330cff3e8114855b0393e380f224ecc\"},{\"spec\":[],\"name\":\"云 宁夏盐池滩羊-羊排 400g\",\"categoryCode\":\"160dd180201c4250b2ad5fc63518efeb\",\"code\":\"e6cfcb7d515e4cbb809a2b0e9f8aa811\"},{\"spec\":[{\"specCode\":\"201508260000000024\",\"specName\":\"份\"}],\"name\":\"春菜 350g\",\"categoryCode\":\"3487e7975ae44350aef6d39d8895ce80\",\"code\":\"f4f13b02b6174e77b933c7e3289612a6\"}],\"mcode\":\"synGoodsStockNums\"}";
            byte[] ksksk = jsonstr.getBytes();
            JSONObject jsonObject = JSON.parseObject(new String(ksksk,"utf-8").trim());
            System.out.println(jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.renderJson200();

    }
}
