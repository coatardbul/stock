package com.coatardbul.stock.controller;


import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.model.bo.ThsPriceBO;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.service.StockPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * <p>
 * Note: 总分行报告列表
 * <p> TENANT_ID
 * Date: 2021/6/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@RequestMapping("/price")
public class BankReportController {
    @Autowired
    StockPriceService stockPriceService;

//    @WebLog(value = "")
//    @RequestMapping(path = "/addStockPrice", method = RequestMethod.POST)
//    public void  addStockPrice(@Validated @RequestBody StockPriceRequestDTO dto) {
//        stockPriceService.stockPriceProcess(dto);
//    }
    @WebLog(value = "")
    @RequestMapping(path = "/refreshStockPrice", method = RequestMethod.POST)
    public void  refreshStockPrice(@Validated @RequestBody StockPriceRequestDTO dto) {
        stockPriceService.refreshStockPrice(dto);
    }

}
