package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/4
 *
 * @author Su Xiaolei
 */
@Data
public class StockTradeBO {

    /**
     * 6为代码
     */
   private String  stockCode;
    /**
     * 2.45
     */
    private String   price;
    /**
     * 100为一手
     */
    private String   amount;

    /**
     * 卖 S
     * 买 B
     */
    private String  tradeType;

    /**
     * 名称
     */
    private String zqmc;

    /**
     * ？？0289557754  不知道，根据持仓信息查询吧，非必填
     */
    private String gddm;

    /**
     *  SA  深圳A  非必填
     */
    private String market;
}
