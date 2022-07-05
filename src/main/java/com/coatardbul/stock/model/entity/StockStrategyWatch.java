package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockStrategyWatch {
    private String id;

    private String templatedId;


    private String templatedName;

    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 1.已购股票
     * 2.定时任务策略扫描
     * 3.需要发送邮件
     * 4.买入，卖出
     * 5.策略模拟
     */
    private Integer type;

    /**
     * 是否开启交易
     */
    private Integer isOpenTrade;

    /**
     * 1. 必须全程开启
     * 2.=----
     * 3.----
     */
    private Integer watchLevel;

    /**
     * 策略标识，固定策略信息
     */
    private String strategySign;

    /**
     * 买入条件
     */
    private String buyCondition;
}