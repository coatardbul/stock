package com.coatardbul.stock.model.bo;

import lombok.Data;
import org.quartz.Job;
import org.quartz.JobDataMap;

import java.util.Date;

/**

 * @Version 1.0
 * @Description 任务实体
 */
@Data
public class QuartzBean {

    /** 任务id */
    private String  id;

    /** 任务名称 */
    private String jobName;

    /** 任务组 */
    private String jobGroupName;

    /** 任务执行类 */
    private String jobClass;

    /** 任务状态 启动还是暂停*/
    private String jobStatus;


    private String triggerKey;
    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务循环间隔-单位：分钟
     */
    private Integer interval;

    /**
     * 任务结束时间
     */
    private Date endTime;

    /** 任务运行时间表达式 */
    private String cronExpression;

    private String description;

    private JobDataMap jobDataMap;
}