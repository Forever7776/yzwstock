package com.yzw.stock.model;

import com.jfinal.ext.kit.ModelExt;
import com.jfinal.ext.plugin.tablebind.TableBind;

/**
 * Created by liuyj on 2015/10/12.
 */
@TableBind(tableName = "yzwstock_out_storage_address")
public class OutStorageAddress extends ModelExt<OutStorageAddress> {
    public static final OutStorageAddress dao = new OutStorageAddress();
}
