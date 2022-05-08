package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/10
 *
 * @author Su Xiaolei
 */
@Data
public class ThemeBaseInfoBO {

    private String dateStr;


    /**
     * 集合竞价涨幅
     */
    private BigDecimal callAuctionIncreaseRate;
    /**
     * 集合竞价金额
     */
    private BigDecimal callAuctionTradeAmount;

    /**
     * 交易金额
     */
    private BigDecimal tradeAmount;

    /**
     * 收盘涨幅
     */
    private BigDecimal closeIncreaseRate;

    /**
     * 收盘-开盘涨幅
     */
    private BigDecimal subIncreaseRate;




    /**
     * 换手率
     */
    private BigDecimal turnOverRate;


    /**
     * 市值
     */
    private BigDecimal marketValue;

}
