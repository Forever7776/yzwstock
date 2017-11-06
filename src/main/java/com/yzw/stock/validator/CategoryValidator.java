package com.yzw.stock.validator;

import com.jfinal.core.Controller;
import com.yzw.base.jfinal.ext.Validator;

/**
 * Created by luomhy on 2015/7/23.
 */
public class CategoryValidator extends Validator {
    @Override
    protected void validate(Controller c)
    {
        super.validate(c);
        validateString("category.remarks",true, 0, 50, "备注不能超过50个字符");
        validateString("category.name",false,0,30,  "名字不能超过30个字符");
    }
}
