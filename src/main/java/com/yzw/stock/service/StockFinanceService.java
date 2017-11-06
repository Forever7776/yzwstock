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
public class StockFinanceService {
    public static Logger logger = Logger.getLogger(StockFinanceService.class);
    private Controller<MoveStorage> controller;
    public void StockFinanceChooseTableTempateExport(int templateNo, String templatePath, List<Stock> stockList, Account account, HttpServletResponse response, Map<String,Object> param){
        HSSFWorkbook workbook = null;
        String outFileName = "";
        try {
            switch (templateNo){
                case 1:
                    //出库总单表
                    workbook = new HSSFWorkbook(new FileInputStream(new File(templatePath + "/stock_choose_in_storage_table_template.xls")));
                    workbook = createWorkbookByStockFinanceChoose(workbook, stockList, account, param);

                    if(MapUtils.isNotEmpty(param)&&param.containsKey("date_start")&&param.containsKey("date_end")){
                        outFileName = DateFormatUtils.format((Date) param.get("date_start"), "yyyyMMdd")+"-"+DateFormatUtils.format((Date) param.get("date_end"), "yyyyMMdd")+"-内部管理订单"+".xls";
                    }else{
                        outFileName = "仓库财务统计"+new Date().getTime()+".xls";
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

    public HSSFWorkbook createWorkbookByStockFinanceChoose(HSSFWorkbook workbook,List<Stock> stockList,Account account,Map<String,Object> param){
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
            nCell.setCellValue("仓库财务统计");
        }else {
            nCell.setCellValue(account.getStr("company") + "仓库财务统计");
        }

        //出库类型
        nRow = sheet.getRow(2);
        nCell = nRow.getCell(2);
        nCell.setCellValue("其它出库");
        for(int i=0;i<stockList.size();i++) {
            if(i>9){
                sheet.shiftRows(i, sheet.getLastRowNum(), 1,true,false);
                sheet.createRow(i);
            }else {
                nRow = sheet.getRow(5+i);
            }
            //序号
            nCell = nRow.getCell(0);
            nCell.setCellValue(i+1);
            //仓库名称
            nRow = sheet.getRow(5+i);
            nCell = nRow.getCell(1);
            nCell.setCellValue(stockList.get(i).getStr("stock_name"));
            colNo = 2;
            //创建行
            nRow = sheet.getRow(rowNo++);
            //入库总数
            colNo++;
            nCell = nRow.getCell(colNo++);
            if(StringUtils.isBlank(stockList.get(i).get("nums_in").toString())){
                nCell.setCellValue("");
            }else{
                nCell.setCellValue(stockList.get(i).get("nums_in").toString());
            }

            //入库总金额
            nCell = nRow.getCell(colNo++);
            if(StringUtils.isBlank(stockList.get(i).get("amount_in").toString())){
                nCell.setCellValue("");
            }else{
                nCell.setCellValue(stockList.get(i).get("amount_in").toString());
            }

            //出库总数
            nCell = nRow.getCell(colNo++);
            if(StringUtils.isBlank(stockList.get(i).get("num_out").toString())){
                nCell.setCellValue("");
            }else{
                nCell.setCellValue(stockList.get(i).get("num_out").toString());
            }

            //出库总金额
            nCell = nRow.getCell(colNo++);
            if(StringUtils.isBlank(stockList.get(i).get("amount_out").toString())){
                nCell.setCellValue("");
            }else{
                nCell.setCellValue(stockList.get(i).get("amount_out").toString());
            }

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
        nCell = nRow.getCell(11);
        nCell.setCellValue(account.getStr("fax"));
        workbook.getCreationHelper()
                .createFormulaEvaluator()
                .evaluateAll();
        return workbook;
    }

}
