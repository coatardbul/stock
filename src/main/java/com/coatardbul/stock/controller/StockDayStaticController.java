package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.statistic.dayStatic.StockDayStaticService;
import com.coatardbul.stock.service.statistic.dayStatic.dayBaseChart.StockDayTrumpetCalcService;
import com.coatardbul.stock.service.base.StockStrategyService;
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
@Api(tags = "股票查询")
@RequestMapping("/stockQuery")
public class StockDayStaticController {

    @Autowired
    StockDayTrumpetCalcService stockDayTrumpetCalcService;

    @Autowired
    StockStrategyService stockStrategyService;

    @Autowired
    StockDayStaticService stockDayStaticService;



    /**
     * 同花顺新版问财功能
     *
     * @param dto 基础查询对象，支持id和问句查询
     * @return
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/strategy", method = RequestMethod.POST)
    public CommonResult strategy(@Validated @RequestBody StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        return CommonResult.success(stockStrategyService.strategy(dto));
    }


    /**
     * 刷新某天所有的数据，
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshDay", method = RequestMethod.POST)
    public CommonResult refreshDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
        stockDayStaticService.refreshDay(dto);
        return CommonResult.success(null);
    }
    /**
     * 删除某天所有的数据，
     */
    @WebLog(value = "")
    @RequestMapping(path = "/deleteDay", method = RequestMethod.POST)
    public CommonResult deleteDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
        stockDayStaticService.deleteDay(dto);
        return CommonResult.success(null);
    }


    /**
     * 根据日期间隔刷新所有的数据，间隔超过5天，只工作查询最近的5日数据，并且和当天比较
     * 如果日期为当天以前，判断是交易日，返回最近的交易日
     * 如果是当天的，启动定时任务，并且刷新已经有的数据 ，实时刷新数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshDayRange", method = RequestMethod.POST)
    public CommonResult refreshDayRange(@Validated @RequestBody StockEmotionDayRangeDTO dto) {
        stockDayStaticService.refreshDayRange(dto);
        return CommonResult.success(null);
    }


    @WebLog(value = "")
    @RequestMapping(path = "/forceRefreshDayRange", method = RequestMethod.POST)
    public CommonResult forceRefreshDayRange(@Validated @RequestBody StockEmotionDayRangeDTO dto) {
        stockDayStaticService.forceRefreshDayRange(dto);
        return CommonResult.success(null);
    }

    /**
     *获取日期区间的数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getRangeStatic", method = RequestMethod.POST)
    public CommonResult getRangeStatic(@Validated @RequestBody StockEmotionRangeDayDTO dto) {
        return CommonResult.success(stockDayStaticService.getRangeStatic(dto));
    }

    /**
     * 获取对应时间，对应标识的统计数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getDayStatic", method = RequestMethod.POST)
    public CommonResult getDayStatic(@Validated @RequestBody StockEmotionQueryDTO dto) {
        return CommonResult.success(stockDayStaticService.getDayStatic(dto));
    }


}
