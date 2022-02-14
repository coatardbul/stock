package com.coatardbul.stock.model.bo;

import lombok.Data;

@Data
public class DayStandardMedianStatistic {

    /**
     * 昨日涨停
     */
    private String firstLimitUpId;

    /**
     * 连续两天以上涨停
     */
    private String twoLimitUpOneId;

    /**
     * 创业板涨停
     */
    private String growthLimitUpTwoId;


}