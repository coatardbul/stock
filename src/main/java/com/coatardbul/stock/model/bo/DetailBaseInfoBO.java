package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/10
 *
 * @author Su Xiaolei
 */
@Data
public class DetailBaseInfoBO {

    private String dateStr;

    /**
     * 开盘价格
     */
    private BigDecimal openPrice;

    /**
     * 最高价格
     */
    private BigDecimal highPrice;
    /**
     * 最低价格
     */
    private BigDecimal lowPrice;
    /**
     * 收盘价格
     */
    private BigDecimal closePrice;
    /**
     * 开盘涨幅
     */
    private BigDecimal openIncreaseRate;
    /**
     * 收盘涨幅
     */
    private BigDecimal closeIncreaseRate;
    /**
     * 最高涨幅
     */
    private BigDecimal highIncreaseRate;
    /**
     * 最低涨幅
     */
    private BigDecimal lowIncreaseRate;
    /**
     * 振幅
     */
    private BigDecimal differenceRate;



    /**
     * 交易金额
     */
    private BigDecimal tradeAmount;
    /**
     * 换手率
     */
    private BigDecimal turnOverRate;
    /**
     * 集合竞价金额
     */
    private BigDecimal callAuctionTradeAmount;

    /**
     * 市值
     */
    private BigDecimal marketValue;

}
