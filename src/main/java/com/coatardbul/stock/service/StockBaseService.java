package com.coatardbul.stock.service;
import java.math.BigDecimal;

import com.coatardbul.stock.model.bo.ThsBaseInfoBO;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.model.entity.StockBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

import com.coatardbul.stock.mapper.StockBaseMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StockBaseService extends BaseService{

    @Resource
    private StockBaseMapper stockBaseMapper;


    public void refreshStockBaseInfo(StockPriceRequestDTO dto) {
        stockBaseMapper.deleteByPrimaryKey(dto.getCode());
        stockProcess(dto);
    }

    @Override
    public void parseAndSaveDate(String responseString, StockPriceRequestDTO  dto) throws IOException {
        //json解析
        ObjectMapper objectMapper = new ObjectMapper();
        int beginIndex = responseString.indexOf(":");
        String jsonString = responseString.substring(beginIndex + 1, responseString.length() - 2);
        Map<String, String> map = objectMapper.readValue(jsonString, HashMap.class);
        //获取股票基本信息
        StockBase stockBaseByMap = getStockBaseByMap(map);
        stockBaseMapper.insert(stockBaseByMap);
    }
    @Override
    public String getStockUrl(String code) {
        return "http://d.10jqka.com.cn/v2/realhead/hs_" +
                code +
                "/last.js?callback=quotebridge_v2_realhead_hs_" +
                code +
                "_last";

    }

    private StockBase getStockBaseByMap(Map<String, String> map) {
        StockBase result = new StockBase();
        result.setCode(map.get("5"));
        result.setName(map.get("name"));
        result.setCirculatingStockCapital(new BigDecimal(map.get("402")));
        result.setCirculatingStockValue(new BigDecimal(map.get("3475914")));
        result.setAllStockCapital(new BigDecimal(map.get("407")));
        result.setAllStockValue(new BigDecimal(map.get("3541450")));
        result.setPer(new BigDecimal(map.get("2034120")));
        result.setPbr(new BigDecimal(map.get("592920")));
        return result;

    }


}





