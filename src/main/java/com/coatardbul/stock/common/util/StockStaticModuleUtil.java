package com.coatardbul.stock.common.util;

import com.coatardbul.stock.common.util.JsonUtil;
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

    /**
     * 验证对象标识是否与json符合
     * @param objectSign
     * @param objectJson
     */
    public static void verify(String objectSign, String objectJson) {

        if (MINUTE_EMOTION_STATISTIC.equals(objectSign)) {
            JsonUtil.readToValue(objectJson, MinuteEmotionStaticIdBo.class);
        }
        if ("".equals(objectSign)) {
            JsonUtil.readToValue(objectJson, Object.class);
        }
    }


    public static Class getClassBySign(String objectSign){
        if (MINUTE_EMOTION_STATISTIC.equals(objectSign)) {
           return MinuteEmotionStaticIdBo.class;
        }
        return Object.class;
    }
}
