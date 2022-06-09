package com.coatardbul.stock.service.base;

import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.mapper.ProxyIpMapper;
import com.coatardbul.stock.service.statistic.ProxyIpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HttpService {
    @Autowired
    ProxyIpService proxyIpService;


    public String doGet(String url) {

        //创建HttpClient对象

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(url);

        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        CloseableHttpResponse response = null;
        try {

            response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                //返回json格式

                String res = EntityUtils.toString(response.getEntity());

                return res;

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


    /**
     * 获取头信息
     *
     * @param param
     * @param value
     * @return
     */
    public Header getHead(String param, String value) {
        return new BasicHeader(param, value);
    }

    /**
     * 传入的为对象json
     *
     * @param url
     * @param jsonString
     * @param headerList
     * @return
     */
    public String doPost(String url, String jsonString, List<Header> headerList) throws ConnectTimeoutException {
        return doPost(url, jsonString, headerList, true);
    }




    public   String   doPost(String url, String jsonString, List<Header> headerList, boolean isProxy) throws ConnectTimeoutException {
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建Post请求
        HttpPost httpPost = new HttpPost(url);
        //默认json，可以覆盖
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
        //有代理
        HttpHost proxy = null;
        if (isProxy) {
            proxy = proxyIpService.getNewProxyHttpHost();
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(4000)
                    .setConnectionRequestTimeout(4000)
                    .setSocketTimeout(4000)
                    .setProxy(proxy).build();
            httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
        } else {
            // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
            httpClient = HttpClientBuilder.create().build();
        }

        if (headerList != null && headerList.size() > 0) {
            for (Header headerTemp : headerList) {
                httpPost.setHeader(headerTemp);
            }
        }
        StringEntity entity = new StringEntity(jsonString, "UTF-8");
        // post请求是将参数放在请求体里面传过去的;这里将entity放入post请求体中
        httpPost.setEntity(entity);
//        log.info("请求地址："+httpPost.toString()+"请求头信息："+ Arrays.toString(httpPost.getAllHeaders())+"请求体："+jsonString);
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            log.info("结果响应："+httpPost.toString() + "响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
//                log.info("响应内容长度为:" + responseEntity.getContentLength());
                String responseStr = EntityUtils.toString(responseEntity);
//                log.info("响应内容为:" + responseStr);
                return responseStr;
            }
        } catch (ConnectTimeoutException | HttpHostConnectException e) {
            //删除当前ip，重试
            proxyIpService.deleteByIp(proxy.getHostName());
            throw new ConnectTimeoutException("连接超时");
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
