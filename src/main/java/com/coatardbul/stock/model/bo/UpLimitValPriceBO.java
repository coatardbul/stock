package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/4
 *
 * @author Su Xiaolei
 */
@Data
public class UpLimitValPriceBO {


    private String dateStr;
    /**
     * 交易金额
     */
    private BigDecimal tradeAmount;
    /**
     * 涨幅
     */
    private BigDecimal increaseRate;
    /**
     * 换手率
     */
    private BigDecimal turnOverRate;
    /**
     * 集合竞价金额
     */
    private BigDecimal callAuctionTradeAmount;
    /**
     * 集合竞价涨幅
     */
    private BigDecimal callAuctionIncreaseRate;
    /**
     * 集合竞价换手率
     */
    private BigDecimal callAuctionTurnOverRate;

}
