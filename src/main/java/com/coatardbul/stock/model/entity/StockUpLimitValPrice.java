package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
 * 股票涨停两家
 */
@Data
public class StockUpLimitValPrice {
    private String id;

    /**
     * 股票代码
     */
    private String code;

    /**
     * 股票名称
     */
    private String name;

    /**
     * 开始日期
     */
    private String beginDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 对象信息
     */
    private String objectArray;

    /**
     * 每日涨停强弱分析
     */
    private String strongWeakArray;
}