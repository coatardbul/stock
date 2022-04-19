package com.coatardbul.stock.task;

import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.feign.StockTemplateQueryDTO;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
import com.coatardbul.stock.service.statistic.StockUpLimitValPriceService;
import com.coatardbul.stock.service.statistic.dayStatic.StockDayStaticService;
import com.coatardbul.stock.service.statistic.dayStatic.dayBaseChart.StockDayTrumpetCalcService;
import com.coatardbul.stock.service.statistic.dayStatic.scatter.StockScatterService;
import com.coatardbul.stock.service.statistic.dayStatic.scatter.ScatterDayUpLimitCallAuctionService;
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
    ScatterDayUpLimitCallAuctionService stockScatterUpLimitService;
    @Autowired
    StockUpLimitValPriceService stockUpLimitValPriceService;


    @Autowired
    StockSpecialStrategyService stockSpecialStrategyService;

    @XxlJob("dayUpDownJobHandler")
    public void dayUpDownJobHandler() throws IllegalAccessException, ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新每日涨跌统计数据开始" + param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            log.info("刷新每日涨跌统计数据参数" + JsonUtil.toJson(dto));
            stockDayStaticService.refreshDay(dto);
        }
        log.info("刷新每日涨跌统计数据结束");
    }


    @XxlJob("dayTwoUpLimitJobHandler")
    public void dayTwoUpLimitJobHandler() throws IllegalAccessException, ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新两板以上集合竞价数据开始" + param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            log.info("刷新两板以上集合竞价数据参数" + JsonUtil.toJson(dto));
            stockScatterUpLimitService.refreshDay(dto);
        }
        log.info("刷新两板以上集合竞价数据开始");
    }


    @XxlJob("dayTwoAboveUpLimitVolPriceJobHandler")
    public void dayTwoAboveUpLimitVolPriceJobHandler() throws ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新两板以上量价关系开始" + param);
        if (StringUtils.isNotBlank(param)) {
            StockStrategyQueryDTO dto = JsonUtil.readToValue(param, StockStrategyQueryDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            log.info("刷新两板以上量价关系参数" + JsonUtil.toJson(dto));
            stockUpLimitValPriceService.dayTwoAboveUpLimitVolPriceJobHandler(dto);
        }
        log.info("刷新两板以上量价关系结束");
    }


    @XxlJob("dayMarketValueUpLimitJobHandler")
    public void dayMarketValueUpLimitJobHandler() throws IllegalAccessException, ParseException {
        String param = XxlJobHelper.getJobParam();
        log.info("刷新涨停市值散点数据开始" + param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            log.info("刷新涨停市值散点数据参数" + JsonUtil.toJson(dto));
            stockScatterService.refreshDay(dto);

        }
        log.info("刷新涨停市值散点数据结束");
    }


    @XxlJob("openStockOneJobHandler")
    public void openStockOneJobHandler() {
        log.info("刷新开盘首板数据异常类型1开始");
        String param = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(param)) {
            StockStrategyQueryDTO dto = JsonUtil.readToValue(param, StockStrategyQueryDTO.class);
            if (!StringUtils.isNotBlank(dto.getDateStr())) {
                dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            }
            stockSpecialStrategyService.amAbOne(dto);
        }
        log.info("刷新开盘首板数据异常类型1结束");

    }

    @XxlJob("openStockTwoJobHandler")
    public void openStockTwoJobHandler() {
        log.info("刷新开盘首板数据异常类型2开始");
        String param = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(param)) {
            StockStrategyQueryDTO dto = JsonUtil.readToValue(param, StockStrategyQueryDTO.class);
            if (!StringUtils.isNotBlank(dto.getDateStr())) {
                dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            }
            stockSpecialStrategyService.amAbTwo(dto);
        }
        log.info("刷新开盘首板数据异常类型2结束");

    }


}
