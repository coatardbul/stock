package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.dto.StockThemeDayDTO;
import com.coatardbul.stock.model.dto.StockThemeDayRangeDTO;
import com.coatardbul.stock.service.statistic.StockThemeService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "题材查询")
@RequestMapping("/themeStatic")
public class StockThemeStaticController {

    @Autowired
    StockThemeService stockThemeService;

    /**
     * 查询所有的题材
     */
    @WebLog(value = "查询所有的题材")
    @RequestMapping(path = "/getAllTheme", method = RequestMethod.POST)
    public CommonResult getAllTheme(@Validated @RequestBody StockStrategyQueryDTO dto) {
        return null;
    }


    /**
     * 根据题材，日期，刷新当天的竞价数据
     * ？？？尾盘添加当天成交数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshDay", method = RequestMethod.POST)
    public CommonResult refreshDay(@Validated @RequestBody StockThemeDayDTO dto) throws ParseException {
        stockThemeService.refreshDay(dto);
        return CommonResult.success(null);
    }

    /**
     * 删除某天所有的数据，
     */
    @WebLog(value = "")
    @RequestMapping(path = "/deleteDay", method = RequestMethod.POST)
    public CommonResult deleteDay(@Validated @RequestBody StockEmotionDayDTO dto)  {
        return CommonResult.success(null);
    }


    /**
     *
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshDayRange", method = RequestMethod.POST)
    public CommonResult refreshDayRange(@Validated @RequestBody StockThemeDayRangeDTO dto) {
        stockThemeService.refreshDayRange(dto);
        return CommonResult.success(null);
    }
    @WebLog(value = "")
    @RequestMapping(path = "/forceRefreshDayRange", method = RequestMethod.POST)
    public CommonResult forceRefreshDayRange(@Validated @RequestBody StockThemeDayRangeDTO dto) {
        stockThemeService.forceRefreshDayRange(dto);
        return CommonResult.success(null);
    }

    /**
     * 获取日期区间的数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getRangeStatic", method = RequestMethod.POST)
    public CommonResult getRangeStatic(@Validated @RequestBody StockThemeDayRangeDTO dto) {
        return CommonResult.success(stockThemeService.getRangeStatic(dto));
    }


}
