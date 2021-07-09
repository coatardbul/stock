package com.coatardbul.stock.service;

import com.coatardbul.stock.model.bo.ThsPriceBO;
import com.coatardbul.stock.model.bo.YearDayBO;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.coatardbul.stock.mapper.StockPriceMapper;
import com.coatardbul.stock.model.entity.StockPrice;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockPriceService {
    private static final String SPLIT = ",";
    @Resource
    private StockPriceMapper stockPriceMapper;

    public void refreshStockPrice(StockPriceRequestDTO dto) {
        stockPriceMapper.deleteByCodeAndDateBetweenOrEqualTo(dto);
        stockPriceProcess(dto);
    }

    /**
     * 根据对应开始，结束时间，对应额股票代码，获取http请求数据，解析成对应的价格数据，存入表中
     *
     * @param dto
     */
    public void stockPriceProcess(StockPriceRequestDTO dto) {

        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 参数
//        StringBuffer params = new StringBuffer();
//        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
//        params.append("id=" + num);

        // 创建Post请求
        HttpGet httpPost = new HttpGet(getStockPriceUrl(dto.getCode()));
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
                stockPriceProcess(responseStr, dto);
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


    private String getStockPriceUrl(String code) {
        return "http://d.10jqka.com.cn/v6/line/hs_" +
                code +
                "/01/all.js?hexin-v=A6rEhVkvvr5fKTLSdVxojvC1_RtPGy5zIJ-iGTRjVv2IZ0SNHKt-hfAv8m8H";

    }

    private void stockPriceProcess(String responseString, StockPriceRequestDTO code) throws IOException {
        //json解析
        ObjectMapper objectMapper = new ObjectMapper();
        int beginIndex = responseString.indexOf("{");
        String jsonString = responseString.substring(beginIndex, responseString.length() - 1);
        ThsPriceBO thsPriceBO = objectMapper.readValue(jsonString, ThsPriceBO.class);
        //将数据存入表中
        stockPriceProcess(thsPriceBO, code);
    }

    void stockPriceProcess(ThsPriceBO thsPriceBO, StockPriceRequestDTO code) {
        List<StockPrice> stockPriceList = getStockPriceList(thsPriceBO, code);
        //过滤list 中的时间数据
        filterDate(stockPriceList, code);
        for (StockPrice insertObject : stockPriceList) {
            stockPriceMapper.insert(insertObject);
        }
    }

    /**
     * 过滤list 中的时间数据
     *
     * @param stockPriceList
     * @param code
     */
    private void filterDate(List<StockPrice> stockPriceList, StockPriceRequestDTO code) {
        if (StringUtils.isNotBlank(code.getBeginDate()) && StringUtils.isNotBlank(code.getEndDate())) {
            stockPriceList = stockPriceList.stream().filter(o1 -> Integer.parseInt(code.getEndDate()) - Integer.parseInt(o1.getDate()) >= 0 &&
                    Integer.parseInt(code.getBeginDate()) - Integer.parseInt(o1.getDate()) <= 0).collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(code.getBeginDate())) {
            stockPriceList = stockPriceList.stream().filter(o1 -> Integer.parseInt(code.getBeginDate()) - Integer.parseInt(o1.getDate()) <= 0).collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(code.getEndDate())) {
            stockPriceList = stockPriceList.stream().filter(o1 -> Integer.parseInt(code.getEndDate()) - Integer.parseInt(o1.getDate()) >= 0).collect(Collectors.toList());
        } else {

        }
    }

    /**
     * 将爬去的股票信息结息成值
     *
     * @param thsPriceBO
     * @return
     */
    private List<StockPrice> getStockPriceList(ThsPriceBO thsPriceBO, StockPriceRequestDTO code) {
        List<StockPrice> result = new ArrayList<>();
        if (thsPriceBO == null) {
            return result;
        }
        String name = thsPriceBO.getName();
        List<YearDayBO> dayOfYearCountList = getDayOfYearCount(thsPriceBO.getSortYear());
        //成交量
        String[] volumnArray = thsPriceBO.getVolumn().split(SPLIT);
        //最低价，开盘价，最高价，收盘价，价格需要除以100
        String[] priceArray = thsPriceBO.getPrice().split(SPLIT);
        //MMdd
        String[] dateArray = thsPriceBO.getDates().split(SPLIT);

        result = getStockPriceList(code.getCode(), name, priceArray, dateArray, volumnArray, dayOfYearCountList);
        return result;
    }

    private List<StockPrice> getStockPriceList(String code, String name, String[] priceArray, String[] dateArray, String[] volumnArray, List<YearDayBO> dayOfYearCountList) {
        List<StockPrice> result = new ArrayList<>();
        //将年和月放一块
        int index = 0;
        int count = dayOfYearCountList.get(0).getDayOfYear();

        for (int i = 0; i < dateArray.length - 1; i++) {
            StockPrice stockPrice = new StockPrice();
            stockPrice.setCode(code);
            stockPrice.setName(name);
            stockPrice.setOpenPrice(Long.parseLong(priceArray[4 * i + 1]));
            stockPrice.setClosePrice(Long.parseLong(priceArray[4 * i + 3]));
            stockPrice.setMinPrice(Long.parseLong(priceArray[4 * i]));
            stockPrice.setMaxPrice(Long.parseLong(priceArray[4 * i + 2]));
            //TODO
            stockPrice.setLastClosePrice(0L);
            //TODO
            stockPrice.setTurnOverRate(0L);
            //TODO
            stockPrice.setQuantityRelativeRatio(0L);
            //将年月拼接起来
            if (i == count) {
                index++;
                count += dayOfYearCountList.get(index).getDayOfYear();
            }
            stockPrice.setDate(dayOfYearCountList.get(index).getYear().toString() + dateArray[i]);
            stockPrice.setVolumn(Integer.parseInt(volumnArray[i]));
            //价格渲染
            render(stockPrice);
            result.add(stockPrice);
        }
        return result;
    }

    /**
     * 价格渲染
     *
     * @param stockPrice
     */
    private void render(StockPrice stockPrice) {
        stockPrice.setOpenPrice(stockPrice.getMinPrice() + stockPrice.getOpenPrice());
        stockPrice.setClosePrice(stockPrice.getMinPrice() + stockPrice.getClosePrice());
        stockPrice.setMaxPrice(stockPrice.getMinPrice() + stockPrice.getMaxPrice());
    }

    /**
     * 哪一年  ,这一年有多少天
     *
     * @param sortYear
     * @return
     */
    private List<YearDayBO> getDayOfYearCount(List<List<Integer>> sortYear) {
        if (sortYear == null || sortYear.isEmpty()) {
            return null;
        }
        //哪一年  ,这一年有多少天
        List<YearDayBO> result = new ArrayList<>();
        for (List<Integer> dayOfYearList : sortYear) {
            YearDayBO yearDayBO = new YearDayBO();
            yearDayBO.setYear(dayOfYearList.get(0));
            yearDayBO.setDayOfYear(dayOfYearList.get(1));
            result.add(yearDayBO);
        }
        return result;
    }
}
