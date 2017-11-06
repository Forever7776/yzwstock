package com.yzw.base.util.upload;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.upload.UploadFile;
import com.yzw.base.jfinal.ext.ctrl.Controller;
import com.yzw.base.util.Fs;
import com.yzw.base.util.Txt;
import sun.misc.BASE64Decoder;


public class FlashUpload
{
	
	public static final  String PATH="/upload/image/";
	
	/***
	 * 配合 flash upload 插件 使用
	 * 
	 * @param req
	 * @return
	 */
	public static String flashUpload(HttpServletRequest req)
	{

		String path = req.getRealPath(PATH);

		long savePicName = System.currentTimeMillis();

		// String file_src = _savePath + savePicName + "_src.jpg"; //保存原图
		// String filename162 = _savePath + savePicName + "_162.jpg"; //保存162
		// String filename20 = _savePath + savePicName + "_20.jpg"; //保存20

		// String pic=request.getParameter("pic");
		// String pic1=request.getParameter("pic1");
		// String pic3=request.getParameter("pic3");

		String pic2 = req.getParameter("pic2");
		
		// 图2
		try
		{
			
			Fs.writeFile(new File(path + File.separator + savePicName + "_48.jpg"), new BASE64Decoder().decodeBuffer(pic2));
		} catch (IOException e)
		{
			return "{\"status\":0}";
		}

		String picUrl = PATH + savePicName;

		return "{\"status\":1,\"picUrl\":\"" + picUrl + "\"}";

		// if(!pic.equals("")&&pic!=null){
		// //原图
		// File file = new File(file_src);
		// FileOutputStream fout = null;
		// fout = new FileOutputStream(file);
		// fout.write(new BASE64Decoder().decodeBuffer(pic));
		// fout.close();
		// }

		// 图1
		// File file1 = new File(filename162);
		// FileOutputStream fout1 = null;
		// fout1 = new FileOutputStream(file1);
		// fout1.write(new BASE64Decoder().decodeBuffer(pic1));
		// fout1.close();

	}


	/*public static String flashUploadNew(HttpServletRequest req){
		String path = req.getRealPath(PATH);
		long savePicName = System.currentTimeMillis();
		String pic162 = req.getParameter("__avatar1");
		String pic48 = req.getParameter("__avatar2");
		String pic20 = req.getParameter("__avatar3");
		try
		{

			Fs.writeFile(new File(path + File.separator + savePicName + "_162.jpg"), new BASE64Decoder().decodeBuffer(pic162));
			Fs.writeFile(new File(path + File.separator + savePicName + "_48.jpg"), new BASE64Decoder().decodeBuffer(pic48));
			Fs.writeFile(new File(path + File.separator + savePicName + "_20.jpg"), new BASE64Decoder().decodeBuffer(pic20));
		} catch (IOException e)
		{
			return "{\"success\":false,\"msg\":\"保存到服务器失败\"}";
		}

		String picUrl = PATH + savePicName;

		return "{\"success\":true,\"picUrl\":\"" + picUrl + "\"}";
	}*/
	public static String flashUploadNew(Controller cl){
		UploadFile file1 = cl.getFile("__avatar1");
		UploadFile file2 = cl.getFile("__avatar2");
		UploadFile file3 = cl.getFile("__avatar3");
		JSONObject jo = new JSONObject();
		HttpServletRequest request = cl.getRequest();
		try{
			if (file1.getFile().exists()){
				String path = request.getRealPath(PATH);
				long savePicName = System.currentTimeMillis();

				Fs.writeFile(new File(path + File.separator + savePicName + "_100.jpg"), new BASE64Decoder().decodeBuffer(Fs.encodeBase64File(file1.getFile())));
				Fs.writeFile(new File(path + File.separator + savePicName + "_50.jpg"), new BASE64Decoder().decodeBuffer(Fs.encodeBase64File(file2.getFile())));
				Fs.writeFile(new File(path + File.separator + savePicName + "_32.jpg"), new BASE64Decoder().decodeBuffer(Fs.encodeBase64File(file3.getFile())));
				String picUrl = PATH + savePicName;
				jo.put("url",picUrl);
				jo.put("success", true);
			}else{
				jo.put("msg", "上传失败");
				jo.put("success", false);
			}
		}catch (Exception e){
			jo.put("success", false);
			jo.put("msg", "上传文件不符合标准");
		}
		return jo.toJSONString();
	}



}
