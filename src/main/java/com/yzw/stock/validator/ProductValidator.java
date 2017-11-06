package com.yzw.stock.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

/**
 * Created by luomhy on 2015/7/23.
 */
public class ProductValidator extends Validator {
    @Override
    protected void validate(Controller c)
    {
        super.validate(c);
        validateString("product.name",false,0,30,"名字长度为0~30个字符之间");
    }
}
