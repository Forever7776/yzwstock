package com.yzw.stock.service;

import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.RenderException;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.base.util.serial.GenerateSerialNo;
import com.yzw.stock.model.*;
import com.yzw.system.model.Account;
import com.yzw.system.model.User;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by liuyj on 2015/8/17.
 */
public class ReportbadService{
    public static Logger logger = Logger.getLogger(ReportbadService.class);
    private Controller<Reportbad> controller;
    public ReportbadService(Controller<Reportbad> cl){
        this.controller = cl;
    }
    @Before(Tx.class)
    public boolean add(Reportbad reportbad,User user){
        OutStorage outStorage = new OutStorage();
        outStorage.set("aid",controller.getAid());
        Integer out_type=controller.getParaToInt("child.out_type");
        outStorage.set("out_type",out_type);
        outStorage.set("code", GenerateSerialNo.createSerialOutStorageCode());//生成单号数据
        outStorage.set("insert_user_id", user.getId());
        outStorage.saveAndInsertDate();
        Integer inId = outStorage.getId();
        Integer[] productIds = controller.getParaValuesToInt("child.product_id");
        Integer[] productNums = controller.getParaValuesToInt("child.product_nums");
        String[] storeIds = controller.getParaValues("child.stock_id");
        for(int i=0,len=productIds.length;i<len;i++){
            OutStorageDetail detail = new OutStorageDetail();
            reportbad.set("stock_id",storeIds[i]);
            detail.set("outstorage_id",inId);
            detail.set("product_id",productIds[i]);
            detail.set("product_num",productNums[i]);
            detail.set("stock_id",storeIds[i]);
            detail.saveAndInsertDate();
        }
        reportbad.set("outstorage_id",outStorage.getInt("id"));
        reportbad.saveAndInsertDate();
        return true;

    }

    @Before(Tx.class)
    public boolean review(Reportbad reportbad, OutStorage outStorage){
        boolean isPass = reportbad.getInt("review")==1;
        if(!isPass){
            reportbad.set("status", 0);//流程结束
            outStorage.set("status", 0);
            outStorage.updateAndLastDate();
            reportbad.updateAndLastDate();
            return true;
        }
        outStorage.set("status", 3);
        List<OutStorageDetail> details = OutStorageDetail.dao.listByDetail(outStorage.getId());
        int id = outStorage.get("id");
        OutStorage outDetails = OutStorage.dao.findFirst("select * from yzwstock_out_storage  where id=?", id);
        int status=Integer.parseInt(outDetails.get("status").toString());
        if (outStock(outStorage, details)) {
            if (outDetails.get("out_type").equals(7)&&status!=7) {
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
            reportbad.set("status",3);
            reportbad.updateAndLastDate();
            return true;
        }else {
            return outStorage.updateAndLastDate();
        }
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

    @Before(Tx.class)
    public Integer ScanCodeOutStroageProcess(int aid,String product_code,int id){
        Integer result = 1;
        Reportbad reportbad = Reportbad.dao.findById();
        //出库单是否存在
        OutStorage outStorage = OutStorage.dao.findById(reportbad.getInt("outstorage_id"));
        if(outStorage==null){
            result = -1;
        }
        if(result==1){
            //查询被扫码的出库商品
            StockProduct stockProduct = StockProduct.dao.getByCode(product_code);
            //查询出库单详细
            Stock topStock = Stock.dao.getTopParent(stockProduct.getInt("stock_id"));
            OutStorageDetail outStorageDetail = OutStorageDetail.dao.findFirst("select * from yzwstock_out_storage_detail  where outstorage_id = ? and product_id = ? and stock_id in ("+topStock.getInt("id")+")",outStorage.getInt("id"), stockProduct.getInt("product_id"));
            //该出库单中该产品已扫码出库的数
            List<StockProduct> stockProductList = StockProduct.dao.find("select * from yzwstock_stock_product where product_id = ? and outstorage_id = ?",stockProduct.getInt("product_id"),outStorage.getInt("id"));
            if(outStorageDetail==null){
                return -5;
            }else{
                if(stockProductList.size()>=outStorageDetail.getInt("product_num")){
                    return -6;
                }
            }

            if (stockProduct!=null){
                if(stockProduct.getInt("status")==1){
                    stockProduct.set("outstorage_id",outStorage.getInt("id"));
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
    public void ReportbadTableTempateExport(int templateNo,String templatePath,OutStorage outStorage,Account account,HttpServletResponse response,Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //报损总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/reportbad_table_template.xls")));
                    workbook = createWorkbookByOutStorage(workbook, outStorage,account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "报损总单_"+new Date().getTime()+".xls";
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
            nCell.setCellValue("报损总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "报损总单");
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
            //摘要
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("无");

            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("无");

            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("无");

            //单位
            nCell = nRow.getCell(colNo++);
            if(outStorageDetail.getDouble("unit")==null){
                nCell.setCellValue("");
            }else {
                nCell.setCellValue(outStorageDetail.getStr("unit"));
            }
          /*
            //库房
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getStr("stock_name"));*/


            //数量
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(outStorageDetail.getInt("product_num"));

            //单价
            nCell = nRow.getCell(colNo++);
            if(outStorageDetail.getDouble("product_price")==null){
                nCell.setCellValue("");
            }else {
                nCell.setCellValue(outStorageDetail.getDouble("product_price"));
            }
            //为了模板中缺少D列，所以跨过D列
            colNo++;
            //金额
            nCell = nRow.getCell(colNo++);
            if(outStorageDetail.getDouble("product_price")==null){
                nCell.setCellValue("");
            }else{
                nCell.setCellValue(outStorageDetail.getDouble("product_price")*outStorageDetail.getInt("product_num"));
            }

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

        return workbook;
    }

    public void ReportbadChooseTableTempateExport(int templateNo,String templatePath,List<Reportbad> reportbadList,Account account,HttpServletResponse response,Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/reportbad_choose_in_storage_table_template.xls")));
                    workbook = createWorkbookByReportbadChoose(workbook, reportbadList, account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "报损总单_"+new Date().getTime()+".xls";
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

    public HSSFWorkbook createWorkbookByReportbadChoose(HSSFWorkbook workbook,List<Reportbad> reportbadList,Account account,Map<String,Object> param){
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
            nCell.setCellValue("报损总单");
        }else {
            nCell.setCellValue(account.getStr("company") + "报损总单");
        }

        //出库类型
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");
        for(int i=0;i<reportbadList.size();i++) {
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
            nCell.setCellValue(reportbadList.get(i).getStr("name"));
            colNo = 2;
            //创建行
            nRow = sheet.getRow(rowNo++);
            //规格
            colNo++;
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(reportbadList.get(i).getStr("spec"));

            //仓库
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(reportbadList.get(i).getStr("stock_name"));
            //为了模板中缺少D列，所以跨过D列
            //批号
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");
            //仓位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue("");

            //单位
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(reportbadList.get(i).getStr("unit"));

            //数量
            nCell = nRow.getCell(colNo++);
            nCell.setCellValue(Integer.parseInt(reportbadList.get(i).get("nums").toString()));

        }
        //公司地址
        nRow = sheet.getRow(18);
        nCell = nRow.getCell(2);
        nCell.setCellValue(account.getStr("address"));

        //电话
        nRow = sheet.getRow(18);
        nCell = nRow.getCell(6);
        nCell.setCellValue(account.getStr("tel"));
        //传真
        nRow = sheet.getRow(18);
        nCell = nRow.getCell(10);
        nCell.setCellValue(account.getStr("fax"));
        workbook.getCreationHelper()
                .createFormulaEvaluator()
                .evaluateAll();
        return workbook;
    }
}
