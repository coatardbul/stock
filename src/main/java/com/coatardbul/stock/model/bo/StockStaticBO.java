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

    /**
     * 方差
     */
    private BigDecimal variance;
    /**
     * 中位数
     */
    private BigDecimal median;
    /**
     * adjs
     */
    private Integer adjs;



}
