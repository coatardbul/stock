package com.coatardbul.stock.task;

import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.service.statistic.dayBaseChart.StockDayStaticService;
import com.coatardbul.stock.service.statistic.dayBaseChart.StockDayTrumpetCalcService;
import com.coatardbul.stock.service.statistic.scatter.StockScatterService;
import com.coatardbul.stock.service.statistic.scatter.ScatterDayUpLimitService;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@Component
public class DayStatisticJob {
    @Autowired
    StockDayTrumpetCalcService stockDayTrumpetCalcService;

    @Autowired
    StockStrategyService stockStrategyService;
@Autowired
    StockScatterService stockScatterService;
    @Autowired
    StockDayStaticService stockDayStaticService;

    @Autowired
    ScatterDayUpLimitService stockScatterUpLimitService;

    @XxlJob("dayStaticJobHandler")
    public void dayStaticJobHandler() {
        log.info("刷新喇叭口统计数据开始");
        StockExcelStaticQueryDTO stockExcelStaticQueryDTO = new StockExcelStaticQueryDTO();
        stockExcelStaticQueryDTO.setExcelTemplateId("1483051288928321536");
        stockExcelStaticQueryDTO.setDateBeginStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
        stockExcelStaticQueryDTO.setDateEndStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
        log.info("刷新喇叭口统计数据参数"+JsonUtil.toJson(stockExcelStaticQueryDTO));
        stockDayTrumpetCalcService.saveDate(stockExcelStaticQueryDTO);
        log.info("刷新喇叭口统计数据结束");
    }


    @XxlJob("dayUpDownJobHandler")
    public void dayUpDownJobHandler() throws IllegalAccessException, ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新每日涨跌统计数据开始"+param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(),DateTimeUtil.YYYY_MM_DD));
            log.info("刷新每日涨跌统计数据参数"+JsonUtil.toJson(dto));
            stockDayStaticService.refreshDay(dto);
        }
        log.info("刷新每日涨跌统计数据结束");
    }


    @XxlJob("dayTwoUpLimitJobHandler")
    public void dayTwoUpLimitJobHandler() throws IllegalAccessException, ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新两板以上集合竞价数据开始"+param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(),DateTimeUtil.YYYY_MM_DD));
            log.info("刷新两板以上集合竞价数据参数"+JsonUtil.toJson(dto));
            stockScatterUpLimitService.refreshDay(dto);

        }
        log.info("刷新两板以上集合竞价数据开始");
    }


    @XxlJob("dayMarketValueUpLimitJobHandler")
    public void dayMarketValueUpLimitJobHandler() throws IllegalAccessException, ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新涨停市值散点数据开始"+param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(),DateTimeUtil.YYYY_MM_DD));
            log.info("刷新涨停市值散点数据参数"+JsonUtil.toJson(dto));
            stockScatterService.refreshDay(dto);

        }
        log.info("刷新涨停市值散点数据结束");
    }
}
