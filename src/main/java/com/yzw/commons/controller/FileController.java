package com.yzw.commons.controller;

import java.io.IOException;


import com.jfinal.ext.route.ControllerBind;
import com.yzw.base.jfinal.ext.ctrl.JsonController;
import com.yzw.base.util.upload.FlashUpload;
import com.yzw.base.util.upload.KindEditor;

@ControllerBind(controllerKey = "/common/file")
public class FileController extends JsonController{

	public void flashUploadNew() throws IOException{
		getRequest().getInputStream();
		renderJson(FlashUpload.flashUploadNew(this));
	}
	public void flashUpload() throws IOException{
		renderJson(FlashUpload.flashUpload(getRequest()));
	}
	
	public void upload()
	{
		renderJson(KindEditor.upload(this));
	}

	public void fileManage()
	{
		   renderJson(KindEditor.fileManage(getRequest()));
	}

}
