package com.coatardbul.stock.task;

import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.service.statistic.StockDayStaticService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    StockDayStaticService stockDayStaticService;


    @XxlJob("dayStaticJobHandler")
    public void dayStaticJobHandler() {
        log.info("刷新喇叭口统计数据开始");
        StockExcelStaticQueryDTO stockExcelStaticQueryDTO = new StockExcelStaticQueryDTO();
        stockExcelStaticQueryDTO.setExcelTemplateId("1483051288928321536");
        stockExcelStaticQueryDTO.setDateBeginStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
        stockExcelStaticQueryDTO.setDateEndStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
        stockDayStaticService.saveDate(stockExcelStaticQueryDTO);
        log.info("刷新喇叭口统计数据结束");

    }
}
