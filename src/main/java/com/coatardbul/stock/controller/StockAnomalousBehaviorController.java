package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptException;
import java.io.FileNotFoundException;

/**
 * <p>
 * Note:
 * <p>股票异常行为，包含各种行为
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/stockAnomalousBehavior")
public class StockAnomalousBehaviorController {
    @Autowired
    StockSpecialStrategyService stockSpecialStrategyService;


    /**
     *构建昨曾，过去数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/buildLastUpLimitInfo", method = RequestMethod.POST)
    public CommonResult buildLastUpLimitInfo(@Validated @RequestBody StockLastUpLimitDetailDTO dto) throws  NoSuchMethodException, ScriptException, FileNotFoundException {
        stockSpecialStrategyService.buildLastUpLimitInfo(dto);
        return CommonResult.success(null);
    }

    /**
     *强制重新构建
     */
    @WebLog(value = "")
    @RequestMapping(path = "/forceBuildLastUpLimitInfo", method = RequestMethod.POST)
    public CommonResult forceBuildLastUpLimitInfo(@Validated @RequestBody StockLastUpLimitDetailDTO dto)  {
        stockSpecialStrategyService.forceBuildLastUpLimitInfo(dto);
        return CommonResult.success(null);
    }

    /**
     *根据code补充数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/supplementBuildLastUpLimitInfo", method = RequestMethod.POST)
    public CommonResult supplementBuildLastUpLimitInfo(@Validated @RequestBody StockLastUpLimitDetailDTO dto)  {
        stockSpecialStrategyService.supplementBuildLastUpLimitInfo(dto);
        return CommonResult.success(null);
    }




    /**
     *获取过去异动数据数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getAllAnomalousBehaviorData", method = RequestMethod.POST)
    public CommonResult getAllAnomalousBehaviorData(@Validated @RequestBody StockLastUpLimitDetailDTO dto) throws  NoSuchMethodException, ScriptException, FileNotFoundException {
        return CommonResult.success( stockSpecialStrategyService.getAllAnomalousBehaviorData(dto));
    }




}