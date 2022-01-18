package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:统计基本信息
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockStaticBaseBO {
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


}
