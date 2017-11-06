package com.yzw.base.util;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.ext.plugin.config.ConfigKit;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 请求类
 * Created by liuyj on 2015/8/27.
 */
public class StockHttp {
    public static void main(String[] args){
        JSONObject jo = new JSONObject();
        jo.put("mcode", "login");
        jo.put("account","shenliang");
        jo.put("pwd","F59BD65F7EDAFB087A81D4DCA06C4910");
    }

    public static String post(JSONObject jo){
        if(StringUtils.isBlank(jo.getString("mcode"))){
            return null;
        }
        String resultString = null;
        try {
            // 建立连接
            URL url = new URL(ConfigKit.getStr("stock.api.url")+jo.getString("mcode"));// 创建连接
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            //设置连接属性
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            // 设置请求属性
            // 获得数据字节数据，请求数据流的编码，必须和下面服务器端处理请求流的编码一致
            connection.setRequestMethod("POST"); // 设置请求方式
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            // 建立输出流，并写入数据
            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), "UTF-8"); // utf-8编码
            System.out.println("对库存系统请求数据："+jo.toJSONString());
            out.append(jo.toJSONString());
            out.flush();
            out.close();

            // 获得响应状态
            int responseCode = connection.getResponseCode();

            if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功
                // 当正确响应时处理数据
                StringBuffer sb = new StringBuffer();
                String readLine;
                BufferedReader responseReader;
                // 处理响应流，必须与服务器响应流输出的编码一致
                responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                System.out.println("库存系统返回数据：" + sb.toString());
                resultString = sb.toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return resultString;
    }

}
