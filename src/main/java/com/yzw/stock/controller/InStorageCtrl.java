package com.yzw.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.log.Logger;
import com.jfinal.upload.UploadFile;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.EasyuiController;
import com.yzw.base.model.easyui.DataGrid;
import com.yzw.base.model.easyui.Form;
import com.yzw.stock.model.InStorage;
import com.yzw.stock.model.InStorageDetail;
import com.yzw.stock.model.StockProduct;
import com.yzw.stock.service.InStorageService;
import com.yzw.stock.validator.InStroageValidator;
import com.yzw.system.model.Account;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

/*import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;*/

/*import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.rtf.RtfWriter2;*/

/**
 * Created by luomhy on 2015/7/27.
 * 入库管理
 */
@ControllerBind(controllerKey = "/stock/instorage", viewPath = UrlConfig.STOCK)
public class InStorageCtrl extends EasyuiController<InStorage> {
    private Logger logger = Logger.getLogger(this.getClass());

    InStorageService service = new InStorageService(this);

    public Form getFrom(String tableName) {
        return Form.getForm(tableName, this, "code", "supplier_id", "insertDateStart", "insertDateEnd");
    }

    public void list() {
        if (this.isSuperAdmin()) {
            renderJson(InStorage.dao.listByDataGrid(getAid(), getDataGrid(), this.getFrom(InStorage.dao.tableName)));
        } else {
            renderJson(InStorage.dao.listByDataGrid(this.user.getId(), getAid(), getDataGrid(), this.getFrom(InStorage.dao.tableName)));
        }

    }

    @Before(value = {InStroageValidator.class})
    public void add() {
        InStorage inStorage = getModel(InStorage.class, "instorage").set("aid", getAid()).set("status", 1).set("insert_user_id", user.getId()).set("type", 1).set("stock_id",getParaToInt("child.stock_id"));
        boolean result = false;
        try {
            result = service.add(inStorage);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("InStorageCtrl@add error", e);
        }
        renderJsonResult(result);
    }

    @Before(value = {InStroageValidator.class})
    public void edit() {
        renderJsonResult(getModel().updateAndLastDate());
    }

    public void delete() {
        renderJsonResult(InStorage.dao.falseDetele(getPara("id")));
    }

    /**
     * 审批入口
     */
    public void review() {
        InStorage inStorage = getModel(InStorage.class, "instorage");
        inStorage.setDate("review_date");
        inStorage.set("review_user", user.getName());
        inStorage.set("review_user_id", user.getId());
        boolean result = false;
        try {
            result = service.review(inStorage);
        } catch (Exception e) {
            logger.error("InStorageCtrl@review error");
        }
        renderJsonResult(result);
    }

    public void detailPro() {
        InStorage inStorage = getModel(InStorage.class, "instorage");
        inStorage.setDate("review_date");
        inStorage.set("review_user", user.getName());
        inStorage.set("review_user_id", user.getId());
        boolean result = false;
        try {
            result = service.review(inStorage);
        } catch (Exception e) {
            logger.error("InStorageCtrl@review error");
        }

        renderJsonResult(result);
    }

    public void detail() {
        keepPara();
        render("instorage.detail.html");
    }

    public void detail1() {
        keepPara();
        render("instorage.stock.product.detail.html");
    }

    public void detailJson() {
        Integer inId = getParaToInt("inId");
        String product_name = getPara("yzwstock_product.name");
        keepPara();

        DataGrid<InStorageDetail> detailDataGrid = new DataGrid<InStorageDetail>();
        detailDataGrid.sortName = getPara("sort", "");
        detailDataGrid.sortOrder = getPara("order", "");
        detailDataGrid.page = getParaToInt("page", 1);
        detailDataGrid.total = getParaToInt("rows", 15);

        Form form = getFrom(InStorageDetail.dao.tableName);
        if (!StringUtils.isBlank(product_name)) {
            form.addFrom(form, "yzwstock_product.name-*", product_name);
        }

        if (isSuperAdmin()) {
            renderJson(InStorageDetail.dao.listByDetailDataGrid(inId, detailDataGrid, form));
        } else {
            renderJson(InStorageDetail.dao.listByDetailDataGrid(user.getId(), inId, detailDataGrid, form));
        }
    }

    public void detailJson1() {
        Integer inId = getParaToInt("inId");
        String code = getPara("code");
        String product_name = getPara("yzwstock_product.name");
        keepPara();

        Form f = new Form(StockProduct.dao.tableName);
        DataGrid<StockProduct> dataGrid = new DataGrid<StockProduct>();
        dataGrid.sortName = getPara("sort", "");
        dataGrid.sortOrder = getPara("order", "");
        dataGrid.page = getParaToInt("page", 1);
        dataGrid.total = getParaToInt("rows", 15);

        if (isSuperAdmin()) {
            renderJson(StockProduct.dao.listByStockProductDataGrid(inId, code, product_name, dataGrid, f));
        } else {
            renderJson(StockProduct.dao.listByStockProductDataGrid(user.getId(), inId, code, product_name, dataGrid, f));
        }

    }

    public void check() {
        InStorage inStorage = getModel(InStorage.class, "instorage");
        inStorage.setDate("check_date");
        inStorage.set("check_user", user.getName());
        inStorage.set("check_user_id", user.getId());
        boolean result = false;
        try {
            result = service.checkProcess(inStorage);
        } catch (Exception e) {
            logger.error("InStorageCtrl@check error", e);
        }
        renderJsonResult(result);
    }

    public void print() throws Exception{
        Rectangle rectPageSize = new Rectangle(PageSize.A4);
        rectPageSize = rectPageSize.rotate();
        // 创建word文档,并设置纸张的大小
        Document doc = new Document(PageSize.A4);
        String address=ConfigKit.getStr("temp.file.path");
        String fpa="//";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

        //String fileDoc = address+"商品二维码" + System.currentTimeMillis() + ".doc";
        String wordDoc="商品二维码" + sdf.format(new Date()) + ".doc";
        String fileDoc=address+fpa+wordDoc;
        //** 建立一个书写器与document对象关联，通过书写器可以将文档写入到输出流中 */
        RtfWriter2.getInstance(doc, new FileOutputStream(fileDoc));
        doc.open();
        //设置标题字体
        //RtfFont titleFont = new RtfFont("仿宋_GB2312", 16, Font.BOLD, Color.BLACK);
        BaseFont baseFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        //BaseFont baseFont = BaseFont.createFont("C:\\WINDOWS\\Fonts\\SIMSUN.TTC,1", BaseFont.IDENTITY_H,true);
        //BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
        Font font = new Font(baseFont,12, Font.BOLD);
        //设置标题
        Paragraph title = new Paragraph("商品二维码");
        // 设置标题格式对其方式
        title.setAlignment(Element.ALIGN_CENTER);
        title.setFont(font);
        title.setSpacingBefore(20);
        doc.add(title);
        Image png = null;
        String fileName = null;
        Path path = null;
        Table table = null;
        String filePath = ConfigKit.getStr("temp.file.path");
        List<InStorageDetail> inStorageDetailList = InStorageDetail.dao.find("select yzwstock_in_storage_detail.*,yzwstock_product.name,yzwstock_product.spec from yzwstock_in_storage_detail left join yzwstock_product on yzwstock_product.id = yzwstock_in_storage_detail.product_id   where yzwstock_in_storage_detail.instorage_id = ?", this.getPara("id"));
        Paragraph pname = null;
        for (int num = 0; num < inStorageDetailList.size(); num++) {
            InStorageDetail detail = inStorageDetailList.get(num);
            String name = detail.getStr("name") + "(" + detail.get("spec") + ")";
            pname = new Paragraph(name);
            List<StockProduct> list = StockProduct.dao.find("select yzwstock_stock_product.*,yzwstock_product.name,yzwstock_product.spec,yzwstock_stock.name as stock_name from yzwstock_stock_product left join yzwstock_product on yzwstock_stock_product.product_id = yzwstock_product.id " +
                    "left join yzwstock_stock on yzwstock_stock_product.stock_id = yzwstock_stock.id where instorage_id = ? and yzwstock_stock_product.stock_id = ? and  product_id = ?", detail.get("instorage_id"), detail.get("stock_id"), detail.get("product_id"));
            table = new Table(5, 5);
            table.setAlignment(1);
            table.setPadding(0);
            table.setSpacing(0);
            table.setBorderWidth(0);
            table.setWidth(100);
            for (int i = 0; i < list.size(); i++) {
                String code = list.get(i).getStr("code");
                fileName = "" + System.currentTimeMillis() + ".png";
                int width = 200; // 图像宽度
                int height = 200; // 图像高度
                String format = "png";// 图像类型
                Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                BitMatrix bitMatrix = new MultiFormatWriter().encode(String.valueOf(code),
                        BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
                path = FileSystems.getDefault().getPath(filePath, fileName);
                MatrixToImageWriter.writeToPath(bitMatrix, format, path);// 输出图像
                png = Image.getInstance(path.toString());
                //设置图片大小和宽高
                png.scaleAbsolute(110f, 110f);
                //设置图片位置
                png.setAlignment(Image.MIDDLE);
                Cell cell = new Cell(png);
                cell.setBorder(0);
                cell.setBackgroundColor(new Color(255, 255, 255));
                if ((i + 1) % 25 == 0) {
                    pname = new Paragraph(name);
                    pname.setFont(font);
                    pname.setSpacingBefore(2);
                    pname.setAlignment(Element.ALIGN_CENTER);
                    pname .setSpacingBefore(25);
                    pname.setSpacingAfter(5);
                    table.addCell(cell);
                    doc.add(pname);
                    doc.add(table);
                    doc.newPage();
                    table = new Table(5, 5);
                    table.setAlignment(1);
                    table.setPadding(0);
                    table.setSpacing(0);
                    table.setBorderWidth(0);
                    table.setWidth(100);
                } else {
                    table.addCell(cell);
                }
            }
            if (list.size() % 25 != 0) {
                doc.add(pname);
                doc.add(table);
            }
        }
        doc.close();

        //*删除生成的二维码*/
        File dir=new File(filePath);
        File []files=dir.listFiles();
        String filesName="";
        for(File f:files){
            filesName=f.getName();
            if(filesName.endsWith(".png")){
                f.delete();
            }
        }
        renderFile(new File(fileDoc));
    }


     /*
        入库单批量导入模版下载
     */
    public void importDowload(){
        String dowpath = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径;
        service.importExcelDowload(1,dowpath, this.getResponse(),null,getAid());
        this.renderNull();
    }

    /*
    入库单批量导入
     */
    public void importExcel(){
        UploadFile uploadFile = this.getFile("file");
        JSONObject data = new JSONObject();
        try{
            data = service.importExcel(uploadFile, this.getResponse(), null,data);
        }catch (Exception e){
            logger.error("InStorageCtrl@importExcel error", e);
        }
        renderJson(data);
    }


    public void printInStorage(){
        //获得数据
        InStorage inStorage = InStorage.dao.getInStorageData(this.getParaToInt("inid"));
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account account=Account.dao.findFirst("SELECT * FROM  admin_account where id=?", inStorage.get("aid"));
        service.inStorageTableTempateExport(1, path, inStorage,account, this.getResponse(), null);
        this.renderNull();
    }

    public void printInStorageChoose(){
        //获得选择时间数据
        String start=this.getPara("start");
        String end=this.getPara("end");
        //统计模板
        String path = this.getRequest().getServletContext().getRealPath(ConfigKit.getStr("poi.template"));//得到文件夹实际路径
        Account account=Account.dao.findById(getAid());
        List<InStorage> storageList=null;
        if(start.equals(end)){
             storageList=InStorage.dao.find("SELECT sum(t.nums) as nums,t.name,t.spec,t.price,t.unit,t.stock_name,t.insert_date from(select yzwstock_in_storage_detail.nums,yzwstock_product.name,yzwstock_product.spec," +
                     "yzwstock_in_storage_detail.price,yzwstock_product.unit,yzwstock_stock.name as stock_name ,yzwstock_in_storage_detail.insert_date as insert_date from yzwstock_in_storage_detail left join yzwstock_product on yzwstock_product.id = yzwstock_in_storage_detail.product_id " +
                     "left join yzwstock_stock on yzwstock_stock.id = yzwstock_in_storage_detail.stock_id  where yzwstock_stock.aid=? and yzwstock_in_storage_detail.insert_date like '%"+start+"%') t    GROUP BY t.name,t.spec  ORDER BY t.insert_date desc",getAid());
        }else{
             storageList=InStorage.dao.find("SELECT sum(t.nums) as nums,t.name,t.spec,t.price,t.unit,t.stock_name,t.insert_date from(select yzwstock_in_storage_detail.nums,yzwstock_product.name,yzwstock_product.spec," +
                     "yzwstock_in_storage_detail.price,yzwstock_product.unit,yzwstock_stock.name as stock_name ,yzwstock_in_storage_detail.insert_date as insert_date from yzwstock_in_storage_detail left join yzwstock_product on yzwstock_product.id = yzwstock_in_storage_detail.product_id " +
                     "left join yzwstock_stock on yzwstock_stock.id = yzwstock_in_storage_detail.stock_id  where yzwstock_stock.aid=? and yzwstock_in_storage_detail.insert_date >=? and yzwstock_in_storage_detail.insert_date <=?) t   GROUP BY t.name,t.spec  ORDER BY t.insert_date desc",getAid(),start,end);
        }

        service.inStorageChooseTableTempateExport(1, path, storageList, account, this.getResponse(), null);
        this.renderNull();
    }

}