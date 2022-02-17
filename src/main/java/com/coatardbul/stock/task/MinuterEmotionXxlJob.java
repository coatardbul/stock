package com.coatardbul.stock.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
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

    @XxlJob("minuterEmotionJobHandler")
    public void minuterEmotionJobHandler() throws Exception {
        //todo 还是需要传递参数的
        log.info("分钟情绪定时任务开始");
        //ssss


        log.info("分钟情绪定时任务结束");

    }
}
