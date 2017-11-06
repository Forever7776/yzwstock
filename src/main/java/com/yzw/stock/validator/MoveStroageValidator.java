package com.yzw.stock.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

/**
 * Created by admin on 2015/8/26.
 */
public class MoveStroageValidator extends Validator {
    @Override
    protected void validate(Controller c)
    {
        super.validate(c);

        validateString("movetorage.input_name", false, 1, 50, "录入人错误");
        validateString("child.stock_id_in", false, 1, 99999999, "入库仓库不能为空！");
        validateInteger("child.product_id", 1, 9999, "出库产品错误");
        validateDouble("child.price", 0.01, 999999999D, "msg", "出库产品错误");
        validateInteger("child.product_nums",1,9999,"出库产品数量错误");
    }
}
