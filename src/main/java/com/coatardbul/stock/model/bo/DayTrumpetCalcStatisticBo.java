package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/13
 *
 * @author Su Xiaolei
 */
@Data
public class DayTrumpetCalcStatisticBo {
    /**
     * 上涨id
     */
    private String riseId;

    /**
     * 谢跌id
     */
    private String failId;

    /**
     * 主板涨停
     */
    private String firstUpLimitId;

    /**
     * 主板连续两天以上涨停
     */
    private String secondUpLimitId;
    /**
     * 创业板涨停
     */
    private String gemUpLimitId;


}
