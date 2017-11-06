package com.yzw.stock.model;

import com.jfinal.ext.kit.ModelExt;
import com.jfinal.ext.plugin.tablebind.TableBind;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_inventory")
public class Inventory extends ModelExt<Inventory> {
    public static Inventory dao = new Inventory();
}
