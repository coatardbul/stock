package com.coatardbul.stock.service.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class ReportAutoJob extends QuartzJobBean {

    private String id;

    public void setId(String id) {
        this.id = id;
    }
    /**
     * 调度器执行的方法
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("自动任务开始执行");
    }
}
