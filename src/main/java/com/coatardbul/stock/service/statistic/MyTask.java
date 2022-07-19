package com.coatardbul.stock.service.statistic;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;

@DisallowConcurrentExecution   
@Component
public class MyTask extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        JobDataMap map = context.getJobDetail().getJobDataMap();
        String userId = map.getString("userId");
        System.out.println("SimpleJob says: " + jobKey + ", userId: " + userId + " executing at " + new Date());
    }
}