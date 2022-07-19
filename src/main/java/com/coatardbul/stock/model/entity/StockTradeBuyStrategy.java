package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
 * 定时买入模板
 */
@Data
public class StockTradeBuyStrategy {
    private String id;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 开始时间 HH:mm
     */
    private String startTime;

    /**
     * 结束时间：HH:mm
     */
    private String endTime;

    /**
     * 买入金额
     */
    private String allMoney;

    /**
     * 剩余仓位金额
     */
    private String subMoney;

    /**
     * 可以买入的次数
     */
    private Integer allNum;

    /**
     * 剩余次数
     */
    private Integer subNum;

    /**
     * 仓位占比
     */
    private String proportion;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * job定时任务类
     */
    private String jobClass;
}