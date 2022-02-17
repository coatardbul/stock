package com.coatardbul.stock.task;

import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.service.statistic.StockMinuteEmotinStaticService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @XxlJob("minuterEmotionJobHandler")
    public void minuterEmotionJobHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        log.info("分钟情绪定时任务开始,传递参数为：" + param);
        if (StringUtils.isNotBlank(param)) {
            StockEmotionDayDTO stockEmotionDayDTO = JsonUtil.readToValue(param, StockEmotionDayDTO.class);
            stockMinuteEmotinStaticService.taskRefreshDay(stockEmotionDayDTO);
        }
        log.info("分钟情绪定时任务结束");

    }
}
