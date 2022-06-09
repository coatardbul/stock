package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockStrategyWatch {
    private String id;

    private String templatedId;

    private String templatedName;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 1. 必须全程开启
     * 2.=----
     * 3.----
     */
    private Integer watchLevel;

    /**
     * 1.已购股票
     * 2.定时任务策略扫描
     * 3.需要发送邮件
     */
    private Integer type;

    /**
     * 是否开启交易
     */
    private Integer isOpenTrade;
}