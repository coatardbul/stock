package com.coatardbul.stock.task;

import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.service.statistic.StockStrategyWatchService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <p>
 * Note:策略扫描，扫描表中的固定类型的数据
 * <p>
 * Date: 2022/3/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@Component
public class StrategyWatchJob {

    @Autowired
    StockStrategyWatchService stockStrategyWatchService;


    @XxlJob("strategyNowWatchJobHandler")
    public void strategyWatchJobHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        log.info("策略监控定时任务开始,传递参数为：" + param);
        StockEmotionDayDTO stockEmotionDayDTO = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
        stockEmotionDayDTO.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
        stockEmotionDayDTO.setTimeStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.HH_MM));
        stockStrategyWatchService.strategyNowWatch(stockEmotionDayDTO);
        log.info("策略监控定时任务结束");
    }


    @XxlJob("historyStrategyWatchJobHandler")
    public void historyStrategyWatchJobHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        log.info("历史策略监控定时任务开始,传递参数为：" + param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO dto = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            stockStrategyWatchService.simulateHistoryStrategyWatch(dto);
        }
        log.info("历史策略监控定时任务结束");
    }
}
