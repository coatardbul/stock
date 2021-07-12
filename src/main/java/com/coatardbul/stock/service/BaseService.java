package com.coatardbul.stock.service;

import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2021/7/11
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public abstract class BaseService {


    public void stockProcess(StockPriceRequestDTO dto)   {
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 参数
//        StringBuffer params = new StringBuffer();
//        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
//        params.append("id=" + num);

        // 创建Post请求
        HttpGet httpPost = new HttpGet(getStockUrl(dto.getCode()));
        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
        httpPost.setHeader("Referer", "http://www.iwencai.com/");
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            response.setHeader("Content-Type", "text/html; charset=UTF-8");
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            log.info("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                log.info("响应内容长度为:" + responseEntity.getContentLength());
                String responseStr = EntityUtils.toString(responseEntity, "utf-8");
                log.info("响应内容为:" + responseStr);
                //解析response，存入数据
                parseAndSaveDate(responseStr, dto);
            }
        } catch (Exception e) {
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
    }

    protected abstract void parseAndSaveDate(String responseStr, StockPriceRequestDTO dto)throws IOException ;

    protected abstract String getStockUrl(String code);

}