package com.yzw.base.util.serial;


import java.util.Date;

public class DataEveryDaySerialNumber extends EveryDaySerialNumber {

    private String code_name = null;

    public DataEveryDaySerialNumber(int width, String code_name) {
        super(width);
        this.code_name = code_name;
    }

    @Override
    protected int getOrUpdateNumber(Date current, int start) {
        EveryDaySerialNoModel serialNumber = null;
        String date = format(current);
        int num = start;
        if(code_name!=null) {
             serialNumber = EveryDaySerialNoModel.dao.findFirst("select * from system_everyday_serial_number where code_name = ?",code_name);
            if(date.equals(serialNumber.getStr("data_date"))) {
                num = serialNumber.getInt("data_serial_number");
            }
        }
        serialNumber.set("data_date",date);
        serialNumber.set("data_serial_number",num+1);

        if (serialNumber.getId() != null) serialNumber.update();
        else serialNumber.save();
        return num;
    }     
}
