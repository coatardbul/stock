package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStaticTemplateBaseDTO;
import com.coatardbul.stock.service.statistic.StockMinuteEmotinStaticService;
import com.coatardbul.stock.service.statistic.StockStrategyWatchService;
import com.coatardbul.stock.task.DayStatisticJob;
import com.coatardbul.stock.task.MinuterEmotionXxlJob;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/18
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/task")
public class TaskController {

    @Autowired
    DayStatisticJob dayStatisticJob;
    @Autowired
    StockMinuteEmotinStaticService stockMinuteEmotinStaticService;
    @Autowired
    StockStrategyWatchService stockStrategyWatchService;

    @WebLog(value = "")
    @RequestMapping(path = "/dayStatic", method = RequestMethod.POST)
    public CommonResult dayStatic() {
        dayStatisticJob.dayStaticJobHandler();
        return null;
    }


    @WebLog(value = "")
    @RequestMapping(path = "/minuterEmotion", method = RequestMethod.POST)
    public CommonResult minuterEmotion(@Validated @RequestBody StockEmotionDayDTO stockEmotionDayDTO) throws Exception {
        stockMinuteEmotinStaticService.refreshDay(stockEmotionDayDTO);
        return null;
    }

    @WebLog(value = "")
    @RequestMapping(path = "/simulateHistoryStrategyWatch", method = RequestMethod.POST)
    public CommonResult simulateHistoryStrategyWatch(@Validated @RequestBody StockEmotionDayDTO stockEmotionDayDTO) throws Exception {
        stockStrategyWatchService.simulateHistoryStrategyWatch(stockEmotionDayDTO);
        return null;
    }


}
