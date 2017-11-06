package com.yzw.base.util.serial;


/**
   * 刘英俊  20150318
   *
   * 自动生成有规则的编号(或订单号)
   * 生成的格式是: 201503180001 前面几位为当前的日期,后面五位为系统自增长类型的编号
   * 原理: 
   *      1.获取当前日期格式化值;
   *      2.读取文件,上次编号的值+1最为当前此次编号的值
   *      (新的一天会重新从1开始编号)
   *      
   *      存储自动编号值的文件如下(文件名: EveryDaySerialNumber.dat)
   */

public class GenerateSerialNo {
    /*
    public static void main(String[] args) throws InterruptedException{
        while(true) {
        	System.out.println(GenerateSerialNo.createSerialNo());
        	TimeUnit.SECONDS.sleep(2);
       	}
    	
    }
    */

    public static String createSerialSupplierCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(4, "supplier_code");
        return serial.getSerialNumber();
    }

    /**
     * 生成入库单
     * @return
     */
    public static String createSerialInStorageCode(){
        return new DataEveryDaySerialNumber(6, "instorage_code").getSerialNumber();
    }

    /**
     * 生成出库单号
     * @return
     */
    public static String createSerialOutStorageCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(6, "outstorage_code");
        return serial.getSerialNumber();
    }

    /**
     * 生成仓库编号
     * @return
     */
    public static String createSerialStockCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(6, "stock_code");
        return serial.getSerialNumber();
    }

    /**
     * 生成货物编号
     * @return
     */
    public static String createSerialProductCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(6, "product_code");
        return serial.getSerialNumber();
    }

    /**
     * 生成username编号
     * @return
     */
    public static String createSerialUserCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(3, "user_code");
        return serial.getSerialNumber();
    }

    /**
     * 生成报损单编号
     * @return
     */
    public static String createSerialReportBadCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(6, "reportbad_code");
        return serial.getSerialNumber();
    }

    /**
     * 生成货品编号
     * @return
     */
    public static String createSerialProductInCode(){
        SerialNumber serial = new DataEveryDaySerialNumber(6, "product_in_code");
        return serial.getSerialNumber();
    }
}



