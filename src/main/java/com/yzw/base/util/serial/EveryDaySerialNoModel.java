package com.yzw.base.util.serial;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.yzw.base.jfinal.ext.model.Model;

/**
 * Created by liuyj on 2015/6/2.
 */
@TableBind(tableName = "system_everyday_serial_number",pkName = "id")
public class EveryDaySerialNoModel extends Model<EveryDaySerialNoModel> {
    public static final EveryDaySerialNoModel dao = new EveryDaySerialNoModel();
}
