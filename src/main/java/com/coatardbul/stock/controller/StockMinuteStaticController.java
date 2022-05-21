package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.service.statistic.minuteStatic.StockMinuteEmotinStaticService;
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
 * Date: 2022/2/8
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "股票情绪查询")
@RequestMapping("/stockMinuteStatic")
public class StockMinuteStaticController {
    @Autowired
    StockMinuteEmotinStaticService stockMinuteEmotinStaticService;


    /**
     * 根据日期重新刷新所有的数据，
     * 如果日期为当天以前，判断是交易日，返回最近的交易日
     * 如果是当天的，请启动定时任务
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshDay", method = RequestMethod.POST)
    public CommonResult refreshDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException, InterruptedException {
        stockMinuteEmotinStaticService.refreshDay(dto);
        return CommonResult.success(null);
    }

    /**
     * 根据日期重新刷新所有的数据，
     * 如果日期为当天以前，判断是交易日，返回最近的交易日
     * 如果是当天的，请启动定时任务
     */
    @WebLog(value = "")
    @RequestMapping(path = "/quickRefreshDay", method = RequestMethod.POST)
    public CommonResult quickRefreshDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException, InterruptedException {
        stockMinuteEmotinStaticService.quickRefreshDay(dto);
        return CommonResult.success(null);
    }
    @WebLog(value = "")
    @RequestMapping(path = "/quickSaveRedisData", method = RequestMethod.POST)
    public CommonResult quickSaveRedisData(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException, InterruptedException {
        stockMinuteEmotinStaticService.quickSaveRedisData(dto);
        return CommonResult.success(null);
    }

    /**
     * 补充刷新，已经有的数据不会重新刷新
     *
     * @param dto
     * @return
     * @throws IllegalAccessException
     * @throws ParseException
     */
    @WebLog(value = "")
    @RequestMapping(path = "/supplementRefreshDay", method = RequestMethod.POST)
    public CommonResult supplementRefreshDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException, InterruptedException {
        stockMinuteEmotinStaticService.supplementRefreshDay(dto);
        return CommonResult.success(null);
    }

    /**
     * 强制刷新
     *
     * @param dto
     * @return
     * @throws IllegalAccessException
     * @throws ParseException
     */
    @WebLog(value = "")
    @RequestMapping(path = "/forceRefreshDay", method = RequestMethod.POST)
    public CommonResult forceRefreshDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException, InterruptedException {
        stockMinuteEmotinStaticService.forceRefreshDay(dto);
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
        stockMinuteEmotinStaticService.refreshDayRange(dto);
        return CommonResult.success(null);
    }


    @WebLog(value = "")
    @RequestMapping(path = "/supplementRefreshDayRange", method = RequestMethod.POST)
    public CommonResult supplementRefreshDayRange(@Validated @RequestBody StockEmotionDayRangeDTO dto) {
        stockMinuteEmotinStaticService.supplementRefreshDayRange(dto);
        return CommonResult.success(null);
    }

    /**
     * 过滤数据，
     *
     * @param dto
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/filterDate", method = RequestMethod.POST)
    public CommonResult filterDate(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException {
        stockMinuteEmotinStaticService.filterDate(dto);
        return CommonResult.success(null);
    }

    /**
     * 获取对应时间，对应标识的详细数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getDayDetail", method = RequestMethod.POST)
    public CommonResult getDayDetail(@Validated @RequestBody StockEmotionDayDTO dto) {
        return CommonResult.success(stockMinuteEmotinStaticService.getDayDetail(dto));
    }


}
