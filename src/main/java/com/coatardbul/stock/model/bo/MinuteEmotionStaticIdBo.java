package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/11
 *
 * @author Su Xiaolei
 */
@Data
public class MinuteEmotionStaticIdBo {

    /**
     * 实时分钟跌停跌停,累加值
     */
    private String downLimitId;

    /**
     * 实时分钟炸板
     */
    private String explosionUpLimitId;

    /**
     * 实时分钟炸板回封
     */
    private String explosionAndSealId;

    /**
     * 实时分钟涨停
     */
    private String sealUpLimitId;
}
