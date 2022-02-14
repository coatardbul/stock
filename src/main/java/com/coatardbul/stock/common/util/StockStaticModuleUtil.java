package com.coatardbul.stock.common.util;

import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.bo.DayUpDowLimitStatisticBo;
import com.coatardbul.stock.model.bo.MinuteEmotionStaticIdBo;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/11
 *
 * @author Su Xiaolei
 */
public class StockStaticModuleUtil {

    //分钟情绪
    private static final String MINUTE_EMOTION_STATISTIC="minute_emotion_statistic";
    //按天统计，统计涨跌数量信息
    private static final String DAY_UP_DOW_LIMIT_STATISTIC="day_up_dow_limit_statistic";
    //按天统计，统计中位数，标准差
    private static final String DAY_STATIC_STATISTIC="day_static_statistic";

    /**
     * 验证对象标识是否与json符合
     * @param objectSign
     * @param objectJson
     */
    public static void verify(String objectSign, String objectJson) {

        if (MINUTE_EMOTION_STATISTIC.equals(objectSign)) {
            JsonUtil.readToValue(objectJson, MinuteEmotionStaticIdBo.class);
        }
        if (DAY_UP_DOW_LIMIT_STATISTIC.equals(objectSign)) {
            JsonUtil.readToValue(objectJson, DayUpDowLimitStatisticBo.class);
        }
        if (DAY_STATIC_STATISTIC.equals(objectSign)) {
            JsonUtil.readToValue(objectJson, Object.class);
        }
    }


    public static Class getClassBySign(String objectSign){
        if (MINUTE_EMOTION_STATISTIC.equals(objectSign)) {
           return MinuteEmotionStaticIdBo.class;
        }
        if (DAY_UP_DOW_LIMIT_STATISTIC.equals(objectSign)) {
           return DayUpDowLimitStatisticBo.class;
        }
        return Object.class;
    }
}
