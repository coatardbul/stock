package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockDefineStaticDTO;
import com.coatardbul.stock.model.dto.StockUplimitAnalyzeDTO;
import com.coatardbul.stock.model.dto.StockUserCookieDTO;
import com.coatardbul.stock.model.entity.StockUplimitAnalyze;
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
    public CommonResult getUpLimitInfo(@Validated @RequestBody Map dto) throws IllegalAccessException {
        stockUpLimitAnalyzeService.add(dto);
        return CommonResult.success( null);
    }
    @ApiOperation("添加")
    @RequestMapping(path = "/getAll", method = RequestMethod.POST)
    public CommonResult getAll(@Validated @RequestBody StockUplimitAnalyzeDTO dto)  {
        return CommonResult.success( stockUpLimitAnalyzeService.getAll(dto));
    }

    /**
     * 收据数据
     */
    @WebLog(value = "收据数据")
    @RequestMapping(path = "/simulateAdd", method = RequestMethod.POST)
    public CommonResult simulateAdd(@Validated @RequestBody StockDefineStaticDTO dto)  {
        stockUpLimitAnalyzeService.simulateAdd(dto);
        return CommonResult.success(null);
    }



}
