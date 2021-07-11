package com.coatardbul.stock.model.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockPrice {
    /**
     * 板块编码
     */
    private String code;

    /**
     * 日期
     */
    private String date;

    /**
     * 板块名称
     */
    private String name;

    /**
     * 开盘价
     */
    private BigDecimal openPrice;

    /**
     * 收盘价
     */
    private BigDecimal closePrice;

    /**
     * 最低价
     */
    private BigDecimal minPrice;

    /**
     * 最高价
     */
    private BigDecimal maxPrice;

    /**
     * 平开价（即昨日收盘价）
     */
    private BigDecimal lastClosePrice;

    /**
     * 换手率
     */
    private BigDecimal turnOverRate;

    /**
     * 量比
     */
    private BigDecimal quantityRelativeRatio;

    /**
     * 量
     */
    private Integer volumn;
}