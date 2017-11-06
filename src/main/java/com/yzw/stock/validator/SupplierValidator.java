package com.yzw.stock.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

/**
 * Created by liuyj on 2015/7/27.
 */
public class SupplierValidator extends Validator {
    @Override
    protected void validate(Controller c)
    {
        super.validate(c);
        validateString("supplier.name",false,0,30,"名字长度为0~30个字符之间");
        validateString("supplier.phone",false,1,20,"不能为空");
    }
}
