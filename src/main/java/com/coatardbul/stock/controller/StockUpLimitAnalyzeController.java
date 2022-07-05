package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.service.statistic.StockUpLimitAnalyzeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "涨停分析")
@RequestMapping("/upLimitAnalyze")
public class StockUpLimitAnalyzeController {
    @Autowired
    StockUpLimitAnalyzeService stockUpLimitAnalyzeService;

    @ApiOperation("添加")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult getUpLimitInfo(@Validated @RequestBody Map dto)  {
        stockUpLimitAnalyzeService.add(dto);
        return CommonResult.success( null);
    }
    @ApiOperation("添加")
    @RequestMapping(path = "/getAll", method = RequestMethod.POST)
    public CommonResult getAll()  {
        return CommonResult.success( stockUpLimitAnalyzeService.getAll());
    }

}
