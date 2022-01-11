package com.coatardbul.stock.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Slf4j
public class HttpUtil {


    private static int HTTP_DEFAULT_TIMEOUT = 5000;

    public static final String SUCCESS = "success";

    private static URL url;
    private static HttpURLConnection con;
    private static int state = -1;

    public static String send_post_request(String path, String jsonStr, int timeout) throws IOException {
        if (StringUtils.isBlank(path)) {
            return null;
        }

        InputStream inputStream = null;
        OutputStream outStream = null;
        HttpURLConnection conn = null;
        try {
            byte[] entity = jsonStr.getBytes();
            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(timeout);// 设置超时
            conn.setRequestMethod("POST");
            // 允许对外输出数据
            conn.setDoOutput(true);
            // 设定传送的内容类型是可序列化的java对象
            // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
            conn.setRequestProperty("Cookie", "cid=4601c7e068c84fd7b8ec4ca53ed0844a1636026734; ComputerID=4601c7e068c84fd7b8ec4ca53ed0844a1636026734; WafStatus=0; other_uid=Ths_iwencai_Xuangu_qp80gf8eylszu4in9etns7wqykj9d1zj; ta_random_userid=cpyh4nslvl; PHPSESSID=01d818c82a362266f7a648ff03b78977; SL_GWPT_Show_Hide_tmp=1; SL_wptGlobTipTmp=1; user=MDptb181NTg0MDIwOTg6Ok5vbmU6NTAwOjU2ODQwMjA5ODo1LDEsNDA7NiwxLDQwOzcsMTExMTExMTExMTEwLDQwOzgsMTExMTAxMTEwMDAwMTExMTEwMDEwMDEwMDEwMDAwMDAsNDA7MzMsMDAwMTAwMDAwMDAwLDEwNjszNiwxMDAxMTExMTAwMDAxMTAwMTAxMTExMTEsMTA2OzQ2LDAwMDAxMTExMTAwMDAwMTExMTExMTExMSwxMDY7NTEsMTEwMDAwMDAwMDAwMDAwMCwxMDY7NTgsMDAwMDAwMDAwMDAwMDAwMDEsMTA2Ozc4LDEsMTA2Ozg3LDAwMDAwMDAwMDAwMDAwMDAwMDAxMDAwMCwxMDY7NDQsMTEsNDA7MSwxMDEsNDA7MiwxLDQwOzMsMSw0MDsxMDIsMSw0MDoyNDo6OjU1ODQwMjA5ODoxNjQxMjU5ODYzOjo6MTYwODYyNDE4MDoyMjg1Mzc6MDoxZGI4NDBhZDUyYzRmZjVkNTE1MTJkOGYzNjRjY2YzNzY6ZGVmYXVsdF80OjE%3D; userid=558402098; u_name=mo_558402098; escapename=mo_558402098; ticket=ee378fb218485360fe5e7d038981fcd9; user_status=0; utk=c45737de5c108e47ea95e2544891af48; v=AxR6RxP5CRJ3x53ePmUNlsWX41mDbTqoepXM9a72o4AivLpH1n0I58qhnDT9");
            conn.setRequestProperty("AppKey", "3");
            conn.setRequestProperty("Connection", "Keep-Alive");
            outStream = conn.getOutputStream();
            outStream.write(entity);
            if (conn.getResponseCode() == 200) {
                inputStream = conn.getInputStream();
                byte[] dateStream = readStream(inputStream);
                return new String(dateStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                outStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }


    /**
     * 读取流
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }


    /**
     * 获取头信息
     *
     * @param param
     * @param value
     * @return
     */
    public static Header getHead(String param, String value) {
        return new BasicHeader(param, value);
    }


    public static void doPost(String url, String jsonString) throws IOException {
        doPost(url, jsonString, null);
    }


    /**
     * 传入的为对象json
     *
     * @param url
     * @param jsonString
     * @param headerList
     * @return
     */
    public static String doPost(String url, String jsonString, List<Header> headerList) {

        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建Post请求
        HttpPost httpPost = new HttpPost(url);
        //默认json，可以覆盖
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

        if (headerList != null && headerList.size() > 0) {
            for (Header headerTemp : headerList) {
                httpPost.setHeader(headerTemp);
            }
        }
        StringEntity entity = new StringEntity(jsonString, "UTF-8");
        // post请求是将参数放在请求体里面传过去的;这里将entity放入post请求体中
        httpPost.setEntity(entity);


        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            log.info("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                log.info("响应内容长度为:" + responseEntity.getContentLength());
                String responseStr = EntityUtils.toString(responseEntity);
                log.info("响应内容为:" + responseStr);
                return responseStr;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }


}
