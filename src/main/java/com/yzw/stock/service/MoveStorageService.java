package com.yzw.stock.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.RenderException;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.base.util.StockHttp;
import com.yzw.base.util.serial.GenerateSerialNo;
import com.yzw.stock.model.*;
import com.yzw.system.model.Account;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by admin on 2015/8/25.
 */
public class MoveStorageService {
    public static Logger logger = Logger.getLogger(MoveStorageService.class);
    private Controller<MoveStorage> controller;
    public MoveStorageService(Controller<MoveStorage> cl){
        this.controller = cl;
    }

    @Before(Tx.class)
    public boolean add(MoveStorage movestorage,OutStorage outstorage, InStorage inStorage){
        outstorage.set("code", GenerateSerialNo.createSerialOutStorageCode());//生成单号数据
        Integer out_type=controller.getParaToInt("child.out_type");
        outstorage.set("out_type", out_type);
        outstorage.saveAndInsertDate();

        //生成出库单
        Integer outId = outstorage.getId();
        Integer[] productIds = controller.getParaValuesToInt("child.product_id");
        Integer[] productNums = controller.getParaValuesToInt("child.product_nums");
        Integer[] storeIds = controller.getParaValuesToInt("child.stock_id");
        String[] priceStrArr = controller.getParaValues("child.price");

        int priceLen = priceStrArr.length;
        Double[] prices = new Double[priceLen];
        for(int i=0;i<priceLen;i++){
            prices[i] = NumberUtils.toDouble(priceStrArr[i],0D);
        }
        JSONArray array = new JSONArray();
        for(int i=0,len=productIds.length;i<len;i++){
            OutStorageDetail detail = new OutStorageDetail();
            detail.set("outstorage_id",outId);
            detail.set("product_price",prices[i]);
            detail.set("product_id",productIds[i]);
            detail.set("product_num", productNums[i]);
            detail.set("stock_id", storeIds[i]);
            detail.saveAndInsertDate();

            Product product = Product.dao.findById(detail.get("product_id"));
            if(product.get("wx_goods_code")!=null&&product.get("specCode")!=null){
                JSONObject o = new JSONObject();
                o.put("code",product.get("wx_goods_code"));
                o.put("specCode",product.get("specCode"));
                o.put("stocknums",StockProduct.getProdctStockNumsWithExcludelocking(0, product.getId()));
                array.add(o);
            }
        }
        //同步更新微信平台库存数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mcode","synGoodsStockNums");
        jsonObject.put("data",array);
        StockHttp.post(jsonObject);


        //生成入库单
        inStorage.set("code", GenerateSerialNo.createSerialInStorageCode());//生成单号数据
        inStorage.saveAndInsertDate();
        Integer inId = inStorage.getId();
        Integer[] productIds_in = controller.getParaValuesToInt("child.product_id");
        Integer[] productNums_in = controller.getParaValuesToInt("child.product_nums");
        Integer[] storeIds_in = controller.getParaValuesToInt("child.stock_id_in");
        String[] priceStrArr_in = controller.getParaValues("child.price");
        int priceLen_in = priceStrArr_in.length;
        Double[] prices_in = new Double[priceLen_in];
        for(int i=0;i<priceLen_in;i++){
            prices_in[i] = NumberUtils.toDouble(priceStrArr_in[i],0D);
        }
        for(int i=0,len=productIds.length;i<len;i++){
            InStorageDetail detail = new InStorageDetail();
            detail.set("instorage_id",inId);
            detail.set("product_id",productIds_in[i]);
            detail.set("nums",productNums_in[i]);
            detail.set("price",prices_in[i]);
            detail.set("stock_id", storeIds_in[i]);
            detail.saveAndInsertDate();


            Product product = Product.dao.findById(productIds[i]);
            if(product.get("wx_goods_code")!=null&&product.get("specCode")!=null){
                JSONObject o = new JSONObject();
                o.put("code",product.get("wx_goods_code"));
                o.put("specCode",product.get("specCode"));
                o.put("stocknums",StockProduct.getProdctStockNumsWithExcludelocking(0, productIds[i]));
                array.add(o);
            }
        }

        movestorage.set("move_code", GenerateSerialNo.createSerialOutStorageCode());//生成单号数据
        movestorage.set("instorage_id",inStorage.getId()).set("outstorage_id",outstorage.getId());
        movestorage.saveAndInsertDate();
        return true;
    }

    @Before(Tx.class)
    public boolean review(Integer status, String text, MoveStorage moveStorage, OutStorage outStorage, InStorage inStorage){
        moveStorage.set("review_date",new Date());
        moveStorage.set("review_status",status);
        moveStorage.set("review_text",text);
        if(status == 1){
            moveStorage.set("status", 2);//待核验
            outStorage.set("status",2);
            outStorage.set("review",1);
            inStorage.set("status",2);
            inStorage.set("review",1);
        }else{
            moveStorage.set("status", 0);//流程结束
            outStorage.set("status", 0);
            inStorage.set("status", 0);
        }
        moveStorage.updateAndLastDate();
        inStorage.updateAndLastDate();
        outStorage.updateAndLastDate();
        return true;
    }




    public boolean delete( MoveStorage moveStorage, OutStorage outStorage, InStorage inStorage){
        if(moveStorage != null){
            moveStorage.delete();
            outStorage.delete();
            inStorage.delete();
        }
        moveStorage.update();
        inStorage.update();
        outStorage.update();
        return true;
    }

    public boolean check(OutStorage outStorage, MoveStorage moveStorage,InStorage inStorage) {
        inStorage.set("status", 3);
        List<InStorageDetail> details_in = InStorageDetail.dao.listByDetail(inStorage.getId());
        if(goStock(InStorage.dao.findById(inStorage.getInt("id")), details_in)) {
            inStorage.updateAndLastDate();
        }

        outStorage.set("status", 3);
        List<OutStorageDetail> details = OutStorageDetail.dao.listByDetail(outStorage.getId());
        int id = outStorage.get("id");
        OutStorage outDetails = OutStorage.dao.findFirst("select * from yzwstock_out_storage  where id=?", id);
        int status = Integer.parseInt(outDetails.get("status").toString());
        if (outStock(outStorage, details)) {
            if (outDetails.get("out_type").equals(8)&&status!=8) {
                //随机出库（无须扫码出库）
                //查询所有仓库，并得到产品名称各自的数量
                List<Stock> storageList = Stock.dao.find("SELECT   yzwstock_stock_product.* , yzwstock_product.name,count(*) count FROM yzwstock_stock_product LEFT JOIN  yzwstock_product ON yzwstock_stock_product.product_id=yzwstock_product.id    GROUP BY yzwstock_product.name");
                for (int b = 0; b < details.size(); b++) {
                    //循环仓库，得到产品名称各自的数量
                    for (int j = 0; j < storageList.size(); j++) {
                        //产品数量
                        int count = Integer.parseInt(storageList.get(j).get("count").toString());
                        //请求的数量
                        int num = Integer.parseInt(details.get(b).get("product_num").toString());
                        if (details.get(b).get("name").equals(storageList.get(j).get("name")) && count > num) {
                            //满足就随机出库（无须扫码出库）
                            List<StockProduct> products = StockProduct.dao.find("select  * from yzwstock_stock_product where stock_id =? and product_id = ? and status = 1", storageList.get(j).get("stock_id"), storageList.get(j).get("product_id"));
                            int counts = products.size();

                            int product_num =  num;
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
                            outStorage.set("status", 6).saveOrUpdate();//出库完成
                        }
                    }
                }
            }
            moveStorage.set("status", 3);
            moveStorage.updateAndLastDate();
            return true;
        }else {
            moveStorage.set("status", 3);
            moveStorage.updateAndLastDate();
            outStorage.updateAndLastDate();
            return true;
        }
    }

    public boolean outStock(OutStorage outStorage,List<OutStorageDetail> list){
        for (OutStorageDetail detail : list) {
            List<StockProduct> stockProductList = StockProduct.dao.find("select * from yzwstock_stock_product where outstorage_id = ? and product_id = ?",detail.getInt("outstorage_id"),detail.getInt("product_id"));
            for(StockProduct stockProduct :stockProductList){
                stockProduct.set("status", 3);//出库
                stockProduct.setDate("out_time");
                if(!stockProduct.updateAndLastDate()){
                    throw new RuntimeException("outStock error");
                }
            }

        }
        return true;
    }



    @Before(Tx.class)
    public boolean goStock(InStorage inStorage,List<InStorageDetail> list){
        Integer aid = controller.getAid();
        Integer inId = inStorage.getId();
        String inCode = inStorage.getStr("code");
        for (InStorageDetail detail : list) {
            Integer len = detail.getInt("nums");
            Integer stock_id = detail.getInt("stock_id");
            Integer product_id = detail.getInt("product_id");
            Product product = Product.dao.findById(product_id);
            if(product==null){
                throw new RuntimeException(String.format("goStock product[%d] error",product_id));
            }
            String midCode = StringUtils.isBlank(product.getStr("code"))?product.getStr("pinyin"):product.getStr("code");
            for(int i=0;i<len;i++){
                //product_in_code
                String spCode = inCode+"-"+midCode+"-"+GenerateSerialNo.createSerialProductInCode();
                StockProduct stockProduct = new StockProduct();
                stockProduct.set("aid", aid);
                stockProduct.set("code",spCode);
                stockProduct.set("type",1);//入库
                stockProduct.set("instorage_id", inId);
                stockProduct.set("stock_id", stock_id);
                stockProduct.set("product_id", product_id);
                stockProduct.set("status", 1);
                stockProduct.setDate("in_time");
                stockProduct.saveAndInsertDate();
            }

        }
        JSONArray array = new JSONArray();
        for (InStorageDetail detail : list) {
            Product product = Product.dao.findById(detail.get("product_id"));
            if(product.get("wx_goods_code")!=null&&product.get("specCode")!=null){
                JSONObject o = new JSONObject();
                o.put("code",product.get("wx_goods_code"));
                o.put("specCode",product.get("specCode"));
                o.put("stocknums",StockProduct.getProdctStockNumsWithExcludelocking(0, product.getId()));
                array.add(o);
            }
        }
        //同步更新微信平台库存数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mcode","synGoodsStockNums");
        jsonObject.put("data",array);
        StockHttp.post(jsonObject);
        return true;
    }

    public void MoveStorageChooseTableTempateExport(int templateNo, String templatePath, List<MoveStorage> moveStorageList, Account account, HttpServletResponse response, Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/choose_in_storage_table_template.xls")));
                    workbook = createWorkbookByMoveStorageChoose(workbook, moveStorageList, account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "调库总单"+new Date().getTime()+".xls";
                    }
                    break;
            }

            //获取response对象
            response.reset();
            response.setContentType("application/msexcel;charset=UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(outFileName, "UTF-8"));
            OutputStream os = null;
            try {
                os = response.getOutputStream();
                workbook.write(os);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RenderException(e);
            } finally {
                try {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error("orderTempateExport 导出订单出错",e);
        }

    }

    public HSSFWorkbook createWorkbookByMoveStorageChoose(HSSFWorkbook workbook,List<MoveStorage> moveStorageList,Account account,Map<String,Object> param){
        int rowNo = 0;//行号
        int colNo = 0;//列号
        //Create a blank sheet
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFRow nRow = null;
        HSSFCell nCell = null;
        // 创建单元格样式对象
        HSSFCellStyle cellStyle = null;
        CellRangeAddress cellRangeAddress = null;

        //需要填写内容的第 0 行
        nRow = sheet.getRow(0);
        cellStyle =nRow.getRowStyle();

        rowNo = 5;

        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);// 设置字体大小

        //公司名称出库总单
        nRow = sheet.getRow(0);
        nCell = nRow.getCell(0);
        if(StringUtils.isBlank(account.getStr("company"))){
            nCell.setCellValue("调库总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "调库总单");
        }

        //出库类型
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");
        for(int i=0;i<moveStorageList.size();i++) {
            if(i>9){
                sheet.shiftRows(i, sheet.getLastRowNum(), 1,true,false);
                sheet.createRow(i);
            }else {
                nRow = sheet.getRow(5+i);
            }
            //序号
            nCell = nRow.getCell(0);
            nCell.setCellValue(i+1);
            //商品名称
            nRow = sheet.getRow(5+i);
            nCell = nRow.getCell(1);
            nCell.setCellValue(moveStorageList.get(i).getStr("name"));
            colNo = 2;
            //创建行
            nRow = sheet.getRow(rowNo++);
            //规格
            colNo++;
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(moveStorageList.get(i).getStr("spec"));

            //仓库
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(moveStorageList.get(i).getStr("stock_name"));
            //为了模板中缺少D列，所以跨过D列
            //批号
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //仓位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");

            //单位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(moveStorageList.get(i).getStr("unit"));

            //数量
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(Integer.parseInt(moveStorageList.get(i).get("nums").toString()));

            //单价
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(moveStorageList.get(i).getDouble("product_price"));
            //金额
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(moveStorageList.get(i).getDouble("product_price") * Integer.parseInt(moveStorageList.get(i).get("nums").toString()));
        }
        //公司地址
        nRow = sheet.getRow(20);
        nCell = nRow.getCell(2);
        nCell.setCellValue(account.getStr("address"));

        //电话
        nRow = sheet.getRow(20);
        nCell = nRow.getCell(6);
        nCell.setCellValue(account.getStr("tel"));
        //传真
        nRow = sheet.getRow(20);
        nCell = nRow.getCell(11);
        nCell.setCellValue(account.getStr("fax"));
        workbook.getCreationHelper()
                .createFormulaEvaluator()
                .evaluateAll();
        return workbook;
    }

}
