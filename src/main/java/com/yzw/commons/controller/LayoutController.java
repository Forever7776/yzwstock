package com.yzw.commons.controller;

import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.config.UrlConfig;
import com.yzw.base.jfinal.ext.ctrl.Controller;

@ControllerBind(controllerKey = "/layout",viewPath= UrlConfig.LAYOUT)
public class LayoutController extends Controller
{
}
