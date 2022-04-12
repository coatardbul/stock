package com.coatardbul.stock.task;

import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.service.statistic.StockMinuteEmotinStaticService;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
import com.coatardbul.stock.service.statistic.StockVerifyService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
public class MinuterEmotionXxlJob {
    @Autowired
    StockMinuteEmotinStaticService stockMinuteEmotinStaticService;
    @Autowired
    StockSpecialStrategyService stockSpecialStrategyService;

    @Autowired
    StockVerifyService stockVerifyService;

    @XxlJob("minuterEmotionJobHandler")
    public void minuterEmotionJobHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        log.info("分钟情绪定时任务开始,传递参数为：" + param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO stockEmotionDayDTO = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            stockEmotionDayDTO.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            stockEmotionDayDTO.setTimeStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.HH_MM));
            log.info(",传递参数为：" + stockEmotionDayDTO.toString());
            stockMinuteEmotinStaticService.refreshDay(stockEmotionDayDTO);
        }
        log.info("分钟情绪定时任务结束");

    }


    @XxlJob("buildLastUpLimitInfoJobHandler")
    public void buildLastUpLimitInfoJobHandler() throws Exception {
        if (stockVerifyService.isIllegalDate(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD))) {
            return;
        }
        if (stockVerifyService.isIllegalDateTimeStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD),
                DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.HH_MM))) {
            return;
        }

        String param = XxlJobHelper.getJobParam();
        log.info("构造昨曾模式数据定时任务开始,传递参数为：" + param);
        if (StringUtils.isNotBlank(param)) {
            StockLastUpLimitDetailDTO dto = JsonUtil.readToValue(param, StockLastUpLimitDetailDTO.class);
            dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
            log.info("构造昨曾模式数据,传递参数为：" + dto.toString());

            stockSpecialStrategyService.buildLastUpLimitInfo(dto);
        }
        log.info("构造昨曾模式数据定时任务结束");

    }
}
