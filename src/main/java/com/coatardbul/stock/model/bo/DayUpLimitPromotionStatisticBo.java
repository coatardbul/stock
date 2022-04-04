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
public class DayUpLimitPromotionStatisticBo {
    /**
     * 一板昨日涨停数量
     */
    private String firstUpLimitYesterdayId;
    /**
     * 一板当日继续涨停数量
     */
    private String firstUpLimitTodayId;

    /**
     * 二板昨日涨停数量
     */
    private String secondUpLimitYesterdayId;
    /**
     * 二板当日继续涨停数量
     */
    private String secondUpLimitTodayId;

    /**
     * 三板昨日涨停数量
     */
    private String thirdUpLimitYesterdayId;
    /**
     * 三板当日继续涨停数量
     */
    private String thirdUpLimitTodayId;

    /**
     * 四板昨日涨停数量以上
     */
    private String fourUpLimitYesterdayId;
    /**
     * 四板当日继续涨停数量以上
     */
    private String fourUpLimitTodayId;


}
