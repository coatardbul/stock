package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/1
 *
 * @author Su Xiaolei
 */
@Data
public class StockLineInfoBo {


    private String code;

    private String name;


    /**
     * 市值，亿为单位
     */
    private BigDecimal marketValue;


    /**
     * 交易金额，万为单位
     */
    private BigDecimal tradeMoney;

    /**
     * 换手率 70
     */
    private BigDecimal turnoverRate;
}
