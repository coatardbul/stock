package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note
 * <p>
 * Date                             2022/4/10
 *
 * @author Su Xiaolei
 */
@Data
public class UpLimitDetailInfo {

    private String code;
    /**
     * 涨停时间的毫秒时间戳
     */
    private Long time;
    /**
     * 涨停打开时间的毫秒时间戳
     */
    private Long openTime;
    /**
     * 毫秒数量
     */
    private Long duration;
    /**
     * 无用数据
     */
    private Long updatedTime;
    /**
     * 涨停封单量
     */
    private Long firstVol;
    /**
     * 最高封单量
     */
    private Long highestVol;
    /**
     * 涨停封单封成比
     */
    private BigDecimal firstVolDivLTGB;
    private BigDecimal firstVolDivVol;
    private BigDecimal highestVolDivLTGB;
    private BigDecimal highestVolDivVol;
}
