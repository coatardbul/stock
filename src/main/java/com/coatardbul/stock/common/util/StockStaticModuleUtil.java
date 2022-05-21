package com.coatardbul.stock.common.util;

import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.bo.DayTrumpetCalcStatisticBo;
import com.coatardbul.stock.model.bo.DayUpDowLimitStatisticBo;
import com.coatardbul.stock.model.bo.DayUpLimitPromotionStatisticBo;
import com.coatardbul.stock.model.bo.MinuteEmotionStaticIdBo;
import com.coatardbul.stock.model.bo.MinuteUpDownNumStatisticBo;

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
    public static final String MINUTE_EMOTION_STATISTIC="minute_emotion_statistic";
    //分钟涨跌家数
    public static final String MINUTE_UP_DOWN_NUM_STATISTIC="minute_up_down_num_statistic";
    //日喇叭口
    public static final String DAY_TRUMPET_CALC_STATISTIC="day_trumpet_calc_statistic";
    //按天统计，统计涨跌数量信息
    public static final String DAY_UP_DOW_LIMIT_STATISTIC="day_up_dow_limit_statistic";
    //涨停晋级率
    public static final String DAY_UP_LIMIT_PROMOTION_STATISTIC="day_up_limit_promotion_statistic";
    //按天统计，统计市值散点信息
    public static final String DAY_MARKET_VALUE_STATISTIC="day_market_Value_statistic";
    //两板以上集合竞价散点图
    public static final String DAY_CALL_AUCTION_STATISTIC="day_call_auction_statistic";

    //按天统计，统计中位数，标准差
    public static final String DAY_STATIC_STATISTIC="day_static_statistic";

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
        if (DAY_TRUMPET_CALC_STATISTIC.equals(objectSign)) {
            JsonUtil.readToValue(objectJson, DayTrumpetCalcStatisticBo.class);
        }
        if (DAY_UP_LIMIT_PROMOTION_STATISTIC.equals(objectSign)) {
            JsonUtil.readToValue(objectJson, DayUpLimitPromotionStatisticBo.class);
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
        if (DAY_MARKET_VALUE_STATISTIC.equals(objectSign)) {
            return DayUpDowLimitStatisticBo.class;
        }
        if (DAY_CALL_AUCTION_STATISTIC.equals(objectSign)) {
            return DayUpDowLimitStatisticBo.class;
        }
        if (DAY_TRUMPET_CALC_STATISTIC.equals(objectSign)) {
            return DayTrumpetCalcStatisticBo.class;
        }
        if (DAY_UP_LIMIT_PROMOTION_STATISTIC.equals(objectSign)) {
            return DayUpLimitPromotionStatisticBo.class;
        }
        if(MINUTE_UP_DOWN_NUM_STATISTIC.equals(objectSign)){
            return MinuteUpDownNumStatisticBo.class;
        }
        return Object.class;
    }
}
