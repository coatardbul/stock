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
public class DayUpDowLimitStatisticBo {
    /**
     * 一字跌停
     */
    private String downLimitOneWordId;
    /**
     * 跌停跌停,累加值
     */
    private String downLimitId;

    /**
     * 当日炸板股票
     */
    private String explosionUpLimitId;

    /**
     * 当日炸板回封
     */
    private String explosionAndSealId;

    /**
     * 主板涨停
     */
    private String firstUpLimitId;

    /**
     * 主板连续两天以上涨停
     */
    private String secondUpLimitId;
    /**
     * 主板连续三天以上涨停
     */
    private String threeUpLimitId;
    /**
     * 主板连续四天以上涨停
     */
    private String fourUpLimitId;
    /**
     * 上涨id
     */
    private String riseId;

    /**
     * 谢跌id
     */
    private String failId;
}
