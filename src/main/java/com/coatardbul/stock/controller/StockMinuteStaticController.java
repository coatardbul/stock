package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.service.statistic.StockMinuteEmotinStaticService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("/stockMinuteStatic")
public class StockMinuteStaticController {
    @Autowired
    StockMinuteEmotinStaticService stockMinuteEmotinStaticService;


    /**
     * 根据日期刷新所有的数据，
     * 如果日期为当天以前，判断是交易日，返回最近的交易日
     * 如果是当天的，启动定时任务，并且刷新已经有的数据 ，实时刷新数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshDay", method = RequestMethod.POST)
    public CommonResult refreshDay(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException {
        stockMinuteEmotinStaticService.refreshDay(dto);
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


    /**
     * 获取对应时间，对应标识的详细数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getDayDetail", method = RequestMethod.POST)
    public CommonResult getDayDetail(@Validated @RequestBody StockEmotionDayDTO dto) {
        return CommonResult.success( stockMinuteEmotinStaticService.getDayDetail(dto));
    }

    /**
     *
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getRangeDetail", method = RequestMethod.POST)
    public CommonResult getRangeDetail(@Validated @RequestBody StockEmotionDayDTO dto) {
        stockMinuteEmotinStaticService.getRangeDetail(dto);
        return CommonResult.success(null);
    }

    /**
     * 获取对应时间，对应标识的统计数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getRangeStatic", method = RequestMethod.POST)
    public CommonResult getRangeStatic(@Validated @RequestBody StockEmotionQueryDTO dto) {
        return CommonResult.success( stockMinuteEmotinStaticService.getRangeStatic(dto));
    }


}