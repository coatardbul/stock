package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockTradeSellJob {
    private String id;

    /**
     * 股票代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 数据，以100为单位
     */
    private String amount;

    /**
     * 卖出价格
     */
    private String price;

    /**
     * 卖出类型：
     * 1.定时卖出，需要卖出时间
     * 2.条件卖出
     * 3.邮件提醒
     */
    private Integer type;

    /**
     * 卖出日期
     */
    private String sellDate;

    /**
     * 卖出时间
     */
    private String sellTime;

    /**
     * 条件卖出脚本
     */
    private String sellScript;

    /**
     * 卖出状态：1 未卖出 2 已卖出
     */
    private Integer status;
}