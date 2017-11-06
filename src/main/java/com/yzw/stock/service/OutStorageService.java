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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by liuyj on 2015/7/31.
 */
public class OutStorageService {
    public static Logger logger = Logger.getLogger(OutStorageService.class);
    private Controller<OutStorage> controller;
    public OutStorageService(Controller<OutStorage> cl){
        this.controller = cl;
    }
    @Before(Tx.class)
    public boolean add(OutStorage outStorage){
        outStorage.set("code", GenerateSerialNo.createSerialOutStorageCode());//生成单号数据

        outStorage.saveAndInsertDate();

        Integer inId = outStorage.getId();
        Integer[] productIds = controller.getParaValuesToInt("child.product_id");
        String[] productPice = controller.getParaValues("child.product_price");
        Integer[] productNums = controller.getParaValuesToInt("child.product_nums");
        Integer storeId = controller.getParaToInt("child.stock_id");
        Integer out_type=controller.getParaToInt("child.out_type");

        //实时验证是否有足够的库存
        for(int i=0,len=productIds.length;i<len;i++){
            if(!StockProduct.hasFullOutNums(storeId,productIds[i],productNums[i])){
                outStorage.deleteById(inId);
                return false;
            }
        }

        JSONArray array = new JSONArray();

        for(int i=0,len=productIds.length;i<len;i++){
            OutStorageDetail detail = new OutStorageDetail();
            detail.set("outstorage_id",inId);
            detail.set("product_price",Double.parseDouble(productPice[i]));
            detail.set("product_id",productIds[i]);
            detail.set("product_num", productNums[i]);
            detail.set("stock_id", storeId);
            outStorage.set("out_type", out_type);

            detail.saveAndInsertDate();

            Product product = Product.dao.findById(detail.get("product_id"));
            if(product.get("wx_goods_code")!=null&&product.get("specCode")!=null){
                JSONObject o = new JSONObject();
                o.put("code",product.get("wx_goods_code"));
                o.put("specCode",product.get("specCode"));
                o.put("stockid",detail.get("stock_id"));
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

    @Before(Tx.class)
    public boolean review(OutStorage outStorage){
        boolean isPass = outStorage.getInt("review")==1;
        if(isPass){
            outStorage.set("status", 2);//待核验
        }else{
            outStorage.set("status",0);//流程结束
        }
        return outStorage.updateAndLastDate();
    }

    /**
     * 出库数据具体保存到仓库中的数据
     * @param outStorage
     * @param list
     * @return
     */
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

    public boolean checkProcess1(OutStorage outStorage) {
        outStorage.set("status", 3);
        List<OutStorageDetail> details = OutStorageDetail.dao.listByDetail(outStorage.getId());
        if (outStock(outStorage, details)) {
            return outStorage.updateAndLastDate();
        }
        return false;

    }

    public boolean checkProcess(OutStorage outStorage) {
        outStorage.set("status", 3);
        List<OutStorageDetail> details = OutStorageDetail.dao.listByDetail(outStorage.getId());
        int id = outStorage.get("id");
        OutStorage outDetails = OutStorage.dao.findFirst("select * from yzwstock_out_storage  where id=?", id);
        int status=Integer.parseInt(outDetails.get("status").toString());
        if (outStock(outStorage, details)) {
            if (outDetails.get("out_type").equals(6)&&status!=6) {
            //随机出库（无须扫码出库）
            //查询所有仓库，并得到产品名称各自的数量
                List<Stock> storageList = Stock.dao.find("select ysp.stock_id,yp.name,ysp.product_id,count(*) as count from yzwstock_stock_product ysp left join  yzwstock_product yp on ysp.product_id = yp.id GROUP BY ysp.product_id,stock_id");
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
            return true;
             }else {
            return outStorage.updateAndLastDate();
             }
    }


    public Long    getStockNums(Integer aid,Integer productid,Integer stockid){
        List<StockProduct> stockProductList = new ArrayList<StockProduct>();
        Stock stock = Stock.dao.findFirst("select * from yzwstock_stock where id = ? and aid = ?",stockid,aid);

        List<Integer> sonStockIdList = new ArrayList<Integer>();
        sonStockIdList.add(stock.getInt("id"));
        //查询下级
        List<Stock> areaStockList = Stock.dao.find("select * from yzwstock_stock where pid = ?",stock.getInt("id"));
        for(Stock areaStock:areaStockList){
            sonStockIdList.add(areaStock.getInt("id"));
            //查询下级
            List<Stock> daoStockList = Stock.dao.find("select * from yzwstock_stock where pid = ?",areaStock.getInt("id"));
            for(Stock daoStock:daoStockList){
                sonStockIdList.add(daoStock.getInt("id"));
            }
        }
        Iterator iterator =sonStockIdList.iterator();
        String sonStocks = "";
        while (iterator.hasNext()){
            sonStocks += iterator.next().toString();
            if(iterator.hasNext()){
                sonStocks +=",";
            }
        }

        if(StringUtils.isNotBlank(sonStocks)){
            Long nums = StockProduct.dao.getCount("select * from yzwstock_stock_product  where stock_id in (" + sonStocks + ") and product_id = ? and status = 1", productid);
            return nums;
        }
        return 0L;
    }

    @Before(Tx.class)
    public Integer ScanCodeOutStroageProcess(int aid,String product_code,int outid){
        Integer result = 1;
        //出库单是否存在
        OutStorage outStorage = OutStorage.dao.findById(outid);
        if(outStorage==null){
            result = -1;
        }
        if(result==1){
            //查询被扫码的出库商品
            StockProduct stockProduct = StockProduct.dao.getByCode(product_code);
            //查询出库单详细
            Stock topStock = Stock.dao.getTopParent(stockProduct.getInt("stock_id"));
            OutStorageDetail outStorageDetail = OutStorageDetail.dao.findFirst("select * from yzwstock_out_storage_detail  where outstorage_id = ? and product_id = ? and stock_id in ("+topStock.getInt("id")+")",outid, stockProduct.getInt("product_id"));
            //该出库单中该产品已扫码出库的数
            List<StockProduct> stockProductList = StockProduct.dao.find("select * from yzwstock_stock_product where product_id = ? and outstorage_id = ?",stockProduct.getInt("product_id"),outid);
            if(outStorageDetail==null){
                return -5;
            }else{
                if(stockProductList.size()>=outStorageDetail.getInt("product_num")){
                    return -6;
                }
            }

            if (stockProduct!=null){
                if(stockProduct.getInt("status")==1){
                    stockProduct.set("outstorage_id",outid);
                    stockProduct.set("status",2);
                    boolean s = stockProduct.updateAndLastDate();
                    return s?1:-2;
                }else if(stockProduct.getInt("status")==2){
                    result =-3;
                }else{
                    result =-4;
                }

            }else{
                result =-5;
            }
        }
        return result;
    }

    /**
     * @param templateNo
     * @param templatePath
     * @param response
     * @param param
     */
    public void outStorageTableTempateExport(int templateNo,String templatePath,OutStorage outStorage,Account account,HttpServletResponse response,Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/out_storage_table_template.xls")));
                    workbook = createWorkbookByOutStorage(workbook, outStorage,account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "出库总单_"+new Date().getTime()+".xls";
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

    public HSSFWorkbook createWorkbookByOutStorage(HSSFWorkbook workbook,OutStorage outStorage,Account account,Map<String,Object> param){

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

        rowNo = 6;
        nRow = sheet.getRow(rowNo);

        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);// 设置字体大小

        //公司名称出库总单
        nRow = sheet.getRow(0);
        nCell = nRow.getCell(0);
        if(StringUtils.isBlank(account.getStr("company"))){
            nCell.setCellValue("出库总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "出库总单");
        }

        //单号信息
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(12);
        nCell.setCellValue(outStorage.getStr("code"));



        //出库类型
        nRow = sheet.getRow(3);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");

        List<OutStorageDetail> outStorageDetailList = outStorage.get("detailList");
        for(OutStorageDetail outStorageDetail : outStorageDetailList){

            //仓库
            nRow = sheet.getRow(2);
            nCell = nRow.getCell(2);
            nCell.setCellValue(outStorageDetail.getStr("stock_name"));
            colNo = 1;
            //创建行
            nRow = sheet.getRow(rowNo++);

            //序号
            //nCell = nRow.createCell(colNo++);
            //nCell.setCellValue(outStorageDetail.getInt("id"));
            //nCell.setCellStyle(cellStyle);

            //编码
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getStr("code"));

            //货物
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getStr("name"));
            //为了模板中缺少D列，所以跨过D列
            colNo++;
            //规格
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getStr("spec"));
            //辅助属性
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //批号
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //仓位
            nCell = nRow.getCell(colNo++);
            //nCell.setCellValue(outStorageDetail.getStr("stock_name"));
            nCell.setCellValue("");
            //单位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getStr("unit"));
            //数量
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getInt("product_num"));

            //单价
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getDouble("product_price"));
            //为了模板中缺少D列，所以跨过D列
            colNo++;
            //金额
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getDouble("product_price")*outStorageDetail.getInt("product_num"));


            //公司地址
            nRow = sheet.getRow(21);
            nCell = nRow.getCell(2);
            nCell.setCellValue(account.getStr("address"));

            //电话
            nRow = sheet.getRow(21);
            nCell = nRow.getCell(7);
            nCell.setCellValue(account.getStr("tel"));
            //传真
            nRow = sheet.getRow(21);
            nCell = nRow.getCell(12);
            nCell.setCellValue(account.getStr("fax"));

        }
        workbook.getCreationHelper()
                .createFormulaEvaluator()
                .evaluateAll();
        return workbook;
    }

    public void OutStorageChooseTableTempateExport(int templateNo,String templatePath,List<OutStorage> outStorageList,Account account,HttpServletResponse response,Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/choose_in_storage_table_template.xls")));
                    workbook = createWorkbookByOutStorageChoose(workbook, outStorageList, account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "出库总单_"+new Date().getTime()+".xls";
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

    public HSSFWorkbook createWorkbookByOutStorageChoose(HSSFWorkbook workbook,List<OutStorage> outStorageList,Account account,Map<String,Object> param){
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
            nCell.setCellValue("出库总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "出库总单");
        }

        //出库类型
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");
        for(int i=0;i<outStorageList.size();i++) {
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
            nCell.setCellValue(outStorageList.get(i).getStr("name"));
            colNo = 2;
            //创建行
            nRow = sheet.getRow(rowNo++);
            //规格
            colNo++;
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageList.get(i).getStr("spec"));

            //仓库
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageList.get(i).getStr("stock_name"));
            //为了模板中缺少D列，所以跨过D列
            //批号
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //仓位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");

            //单位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageList.get(i).getStr("unit"));

            //数量
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(Integer.parseInt(outStorageList.get(i).get("nums").toString()));

            //单价
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageList.get(i).getDouble("product_price"));
            //金额
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageList.get(i).getDouble("product_price") * Integer.parseInt(outStorageList.get(i).get("nums").toString()));
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
