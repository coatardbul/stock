package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockStaticBO {
    private String dateStr;


    /**
     * 标准差
     */
    private BigDecimal standardDeviation;


    /**
     * 中位数
     */
    private BigDecimal median;

    /**
     * 标的数
     */
    private Integer raiseLimitNum;
    /**
     * adjs
     */
    private Integer adjs;

    /**
     * 标准差
     */
    private BigDecimal standardDeviationOne;


    /**
     * 中位数
     */
    private BigDecimal medianOne;

    /**
     * 标的数
     */
    private Integer raiseLimitNumOne;
    /**
     * 标准差
     */
    private BigDecimal standardDeviationTwo;


    /**
     * 中位数
     */
    private BigDecimal medianTwo;

    /**
     * 标的数
     */
    private Integer raiseLimitNumTwo;
}
