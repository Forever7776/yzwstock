package com.yzw.stock.model;

import com.jfinal.ext.kit.ModelExt;
import com.jfinal.ext.plugin.tablebind.TableBind;

/**
 * Created by luomhy on 2015/7/23.
 */
@TableBind(tableName = "yzwstock_movestore")
public class MoveStrore extends ModelExt<MoveStrore> {
    public static MoveStrore dao = new MoveStrore();
}
