package com.coatardbul.stock.model.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDateStatic {
    private String date;

    /**
     * adjs
     */
    private Integer adjs;

    /**
     * 中位数
     */
    private BigDecimal median;

    /**
     * 标准差
     */
    private BigDecimal standardDeviation;

    /**
     * 涨停标的数
     */
    private Integer raiseLimitNum;

    /**
     * 中位数
     */
    private BigDecimal medianOne;

    /**
     * 标准差
     */
    private BigDecimal standardDeviationOne;

    private Integer raiseLimitNumOne;

    private BigDecimal medianTwo;

    private BigDecimal standardDeviationTwo;

    private Integer raiseLimitNumTwo;
}