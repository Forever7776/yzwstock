package com.yzw.stock.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.RenderException;
import com.jfinal.upload.UploadFile;
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

/**
 * Created by luomhy on 2015/7/30.
 */
public class InStorageService {

    private Controller<InStorage> controller;
    public static Logger logger = Logger.getLogger(InStorageService.class);
    public InStorageService(Controller<InStorage> cl){
        this.controller = cl;
    }

    @Before(Tx.class)
    public boolean add(InStorage inStorage){
        inStorage.set("code", GenerateSerialNo.createSerialInStorageCode());//生成单号数据
        inStorage.saveAndInsertDate();
        Integer inId = inStorage.getId();
        Integer[] productIds = controller.getParaValuesToInt("child.product_id");
        Integer[] productNums = controller.getParaValuesToInt("child.nums");
        Integer storeId = controller.getParaToInt("child.stock_id");
        String[] priceStrArr = controller.getParaValues("child.price");
        int priceLen = priceStrArr.length;
        Double[] prices = new Double[priceLen];
        for(int i=0;i<priceLen;i++){
            prices[i] = NumberUtils.toDouble(priceStrArr[i],0D);
        }
        JSONArray array = new JSONArray();
        for(int i=0,len=productIds.length;i<len;i++){
            InStorageDetail detail = new InStorageDetail();
            detail.set("instorage_id",inId);
            detail.set("product_id",productIds[i]);
            detail.set("nums",productNums[i]);
            detail.set("price",prices[i]);
            detail.set("stock_id", storeId);
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
        return true;
    }

    @Before(Tx.class)
    public boolean review(InStorage inStorage){
        boolean isPass = inStorage.getInt("review")==1;
        if(isPass){
            inStorage.set("status", 2);//待核验
        }else{
            inStorage.set("status",0);//流程结束
        }
        return inStorage.updateAndLastDate();
    }

    /**
     * 具体保存到仓库中的数据
     * @param inStorage
     * @param list
     * @return
     */
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
                o.put("stockid",detail.getInt("stock_id"));
                array.add(o);
            }
        }
        //同步更新微信平台库存数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mcode","synGoodsStockNums");
        jsonObject.put("data", array);
        StockHttp.post(jsonObject);
        return true;
    }
    @Before(Tx.class)
    public boolean checkProcess(InStorage inStorage){
        inStorage.set("status", 3);
        List<InStorageDetail> details = InStorageDetail.dao.listByDetail(inStorage.getId());
        if(goStock(InStorage.dao.findById(inStorage.getInt("id")), details)) {
            return inStorage.updateAndLastDate();
        }
        return false;

    }


    public void printStoreWord(String fipath){


    }
    public void importExcelDowload(int templateNo,String dowpath, HttpServletResponse response,Map<String,Object> param,Integer aid){
        HSSFWorkbook wb = null;
        String outFileName = "";
        try{
            switch (templateNo){
                case 1:
            wb = new HSSFWorkbook(new FileInputStream(new File(dowpath + "/instorage_excel.xls")));
            wb = createWorkbookByDowload(wb,aid);

            if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
            }else{
                outFileName = "入库单_"+new Date().getTime()+".xls";
            }
            break;
            }
            response.reset();
            response.setContentType("application/msexcel;charset=UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(outFileName, "UTF-8"));

            OutputStream os = null;
            try {
                os = response.getOutputStream();
                wb.write(os);
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

        }catch (Exception e){
            logger.error("InStorageService@importExcelDowload 导入模版下载出错",e);
        }
    }

    public JSONObject importExcel(UploadFile uploadFile, HttpServletResponse response, Map<String,Object> param,JSONObject data){
        HSSFWorkbook workbook = null;
        try{
            workbook = new HSSFWorkbook(new FileInputStream( uploadFile.getFile()));
            int rowNo = 2;//行号
            int colNo = 0;//列号
            //Create a blank sheet
            HSSFSheet sheet = workbook.getSheetAt(0);
            HSSFRow nRow = null;
            HSSFCell nCell = null;

            nRow = sheet.getRow(0);
            nCell = nRow.getCell(1);
            if(nCell == null){
                data.put("msg","用户ID不存在，请重新下载模版！");
                data.put("result",false);
                return data;
            }
            Integer aid = (int)nCell.getNumericCellValue();
            int nums = 0;
            Double totalFee = 0.00;
            Double price = 0.00;
            for(int i=0;i<sheet.getLastRowNum()+1;i++){
                InStorage inStorage = new InStorage();
                inStorage.set("status", 1).set("type", 1).set("aid",aid);
                inStorage.set("code", GenerateSerialNo.createSerialInStorageCode());//生成单号数据
                inStorage.saveAndInsertDate();
                InStorageDetail detail = new InStorageDetail();
                Integer inId = inStorage.getId();
                detail.set("instorage_id",inId);
                colNo = 0;
                //创建行
                nRow = sheet.getRow(rowNo++);
                if(nRow == null){
                    inStorage.delete();
                    break;
                }
                //仓库
                nCell = nRow.getCell(colNo++);
                if(nCell == null){
                    inStorage.delete();
                    break;
                }
                nCell.getRichStringCellValue();
                //仓库id
                nCell = nRow.getCell(colNo++);
                detail.set("stock_id", (int) nCell.getNumericCellValue());
                //产品名称
                nCell = nRow.getCell(colNo++);
                nCell.getRichStringCellValue();
                //产品id
                nCell = nRow.getCell(colNo++);
                detail.set("product_id", (int) nCell.getNumericCellValue());
                //规格
                nCell = nRow.getCell(colNo++);
                if(nCell == null){
                    inStorage.delete();
                    continue;
                }
                nCell.getRichStringCellValue();
                //数量
                nCell = nRow.getCell(colNo++);
                if(nCell == null){
                    inStorage.delete();
                    continue;
                }else{
                    nums = (int)nCell.getNumericCellValue();
                    detail.set("nums", nums);
                }
                //单价
                nCell = nRow.getCell(colNo++);
                price = Double.valueOf(nCell.getNumericCellValue());
                detail.set("price", price);
                //总金额
                totalFee = nums * price;
                inStorage.set("total_fee",totalFee).updateAndLastDate();
                detail.saveAndInsertDate();

                nCell = nRow.getCell(colNo++);
                if((int)nCell.getNumericCellValue() == 1 || nCell.getStringCellValue().equals("是")){
                    List<InStorageDetail> details = InStorageDetail.dao.listByDetail(inStorage.getId());
                    if(goStock(inStorage, details)){
                        inStorage.set("check",1);
                        inStorage.set("review",1);
                        inStorage.set("status", 3);
                        inStorage.setDate("review_date");
                        inStorage.setDate("check_date");
                        inStorage.updateAndLastDate();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("InStorageService@importExcel 导入入库单出错",e);
            data.put("msg","导入失败！请重新导入");
            data.put("result",false);
            return data;
        }
        data.put("msg","导入成功！");
        data.put("result",true);
        return data;
    }


    public HSSFWorkbook createWorkbookByDowload(HSSFWorkbook wb, Integer  aid){
        int rowNo = 2;//行号
        int colNo = 1;//列号
        //Create a blank sheet
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow nRow = null;
        HSSFCell nCell = null;
        // 创建单元格样式对象
        HSSFCellStyle cellStyle = null;
        CellRangeAddress cellRangeAddress = null;

        //需要填写内容的第 0 行
        nRow = sheet.getRow(0);
        nCell = nRow.createCell(1);
        nCell.setCellValue(aid);

        HSSFFont font = wb.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);// 设置字体大小

        List<Stock> stockList = Stock.dao.list(aid);
        List<Product> productList = Product.dao.listbyProduct(aid);
        for(Stock stock:stockList){
            for(Product product : productList){
                colNo = 0;
                //创建行
                nRow = sheet.createRow(rowNo++);
                //仓库
                nCell = nRow.createCell(colNo++);
                nCell.getStringCellValue();
                nCell.setCellValue(stock.getStr("name"));
                //仓库id
                nCell = nRow.createCell(colNo++);
                nCell.setCellValue(stock.getInt("id"));
                //产品
                nCell = nRow.createCell(colNo++);
                nCell.setCellValue(product.getStr("name"));
                //产品id
                nCell = nRow.createCell(colNo++);
                nCell.setCellValue(product.getInt("id"));
                //规格
                nCell = nRow.createCell(colNo++);
                nCell.setCellValue(product.getStr("spec"));
            }
        }

        return wb;
    }


    public void inStorageTableTempateExport(int templateNo,String templatePath,InStorage inStorage,Account account,HttpServletResponse response,Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/in_storage_table_template.xls")));
                    workbook = createWorkbookByInStorage(workbook, inStorage, account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "入库总单_"+new Date().getTime()+".xls";
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


    public HSSFWorkbook createWorkbookByInStorage(HSSFWorkbook workbook,InStorage inStorage,Account account,Map<String,Object> param){
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
            nCell.setCellValue("入库总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "入库总单");
        }

        //单号信息
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(12);
        nCell.setCellValue(inStorage.getStr("code"));

        //出库类型
        nRow = sheet.getRow(3);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");

        List<InStorageDetail> inStorageDetailList = inStorage.get("detailList");
        for(InStorageDetail inStorageDetail : inStorageDetailList){
            //仓库
            nRow = sheet.getRow(2);
            nCell = nRow.getCell(2);
            nCell.setCellValue(inStorageDetail.getStr("stock_name"));
            colNo = 1;
            //创建行
            nRow = sheet.getRow(rowNo++);

            //序号
            //nCell = nRow.createCell(colNo++);
            //nCell.setCellValue(outStorageDetail.getInt("id"));
            //nCell.setCellStyle(cellStyle);

            //编码
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getStr("code"));

            //货物
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getStr("name"));
            //为了模板中缺少D列，所以跨过D列
            colNo++;
            //规格
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getStr("spec"));
            //辅助属性
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //批号
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //仓位
            nCell = nRow.getCell(colNo++);
            //nCell.setCellValue(inStorageDetail.getStr("stock_name"));
            nCell.setCellValue("");

            //单位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getStr("unit"));

            //数量
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getInt("nums"));

            //单价
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getDouble("price"));
            //为了模板中缺少D列，所以跨过D列
            colNo++;
            //金额
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(inStorageDetail.getDouble("price")*inStorageDetail.getInt("nums"));

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


    public void inStorageChooseTableTempateExport(int templateNo,String templatePath,List<InStorage> storageList,Account account,HttpServletResponse response,Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/choose_in_storage_table_template.xls")));
                    workbook = createWorkbookByInStorageChoose(workbook, storageList, account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "入库总单_"+new Date().getTime()+".xls";
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


    public HSSFWorkbook createWorkbookByInStorageChoose(HSSFWorkbook workbook,List<InStorage> storageList,Account account,Map<String,Object> param){
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
            nCell.setCellValue("入库总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "入库总单");
        }

        //出库类型
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");
        for(int i=0;i<storageList.size();i++) {
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
                nCell.setCellValue(storageList.get(i).getStr("name"));
                colNo = 2;
                //创建行
                nRow = sheet.getRow(rowNo++);
                //规格
                colNo++;
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue(storageList.get(i).getStr("spec"));

                //仓库
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue(storageList.get(i).getStr("stock_name"));
                //为了模板中缺少D列，所以跨过D列
                //批号
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue("");
                //仓位
                nCell = nRow.getCell(colNo++);
                //nCell.setCellValue(inStorageDetail.getStr("stock_name"));
                nCell.setCellValue("");

                //单位
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue(storageList.get(i).getStr("unit"));

                //数量
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue(Integer.parseInt(storageList.get(i).get("nums").toString()));

                //单价
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue(storageList.get(i).getDouble("price"));
                //金额
                nCell = nRow.getCell(colNo++);
                nCell.setCellValue(storageList.get(i).getDouble("price") * Integer.parseInt(storageList.get(i).get("nums").toString()));
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
