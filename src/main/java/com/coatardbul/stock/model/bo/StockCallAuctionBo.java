package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockCallAuctionBo extends StockLineInfoBo{

    /**
     * 交易金额，万为单位
     */
    private BigDecimal compareTradeMoney;

    /**
     * 换手率 70
     */
    private BigDecimal compareTurnoverRate;


    /**
     * 涨幅
     */
    private BigDecimal compareIncreaseRange;

    /**
     * 竞价比率
     */
    private BigDecimal callAuctionRatio;
}
