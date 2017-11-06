package com.yzw.stock.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

/**
 * Created by luomhy on 2015/7/27.
 */
public class OutStroageValidator extends Validator {
    @Override
    protected void validate(Controller c)
    {
        super.validate(c);

        validateString("outstorage.input_name", false, 1, 50, "录入人错误");
        validateString("child.product_id", false, 1, 100, "请选择产品");
        validateString("child.stock_id", false, 1, 100, "请选择产品仓库");
        validateString("outstorage.customer_id",false,1,100,"请选择客户");
    }
}
