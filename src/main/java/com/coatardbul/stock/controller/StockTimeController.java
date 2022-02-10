package com.coatardbul.stock.controller;

import com.coatardbul.stock.service.StockEmotionService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/8
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "股票情绪查询")
@RequestMapping("/stockEmotionQuery")
public class StockTimeController {
    @Autowired
    StockEmotionService stockEmotionService;



}
