package com.yzw.stock.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

/**
 * Created by luomhy on 2015/7/27.
 */
public class InStroageValidator extends Validator {
    @Override
    protected void validate(Controller c)
    {
        super.validate(c);

        validateString("instorage.input_name", false, 1, 50, "录入人错误");
        validateDouble("child.price", 0.01, 999999999D, "msg", "请输入正确的单价！");
        validateInteger("child.nums", 1, 99999999, "请输入正确的数量");
        validateString("child.product_id", false, 1, 100, "请选择产品");
        validateString("child.stock_id", false, 1, 100, "请选择产品仓库");
        validateInteger("instorage.supplier_id", 1, 100, "请选择供应商！");
    }
}
