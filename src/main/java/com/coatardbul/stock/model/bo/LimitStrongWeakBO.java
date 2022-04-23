package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/10
 *
 * @author Su Xiaolei
 */
@Data
public class LimitStrongWeakBO {

    private String dateStr;

    /**
     * 首次涨停时间
     */
    private Date firstUpLimitDate;

    /**
     * 最后涨停时间
     */
    private Date lastUpLimitDate;

    /**
     * 持续时间，单位分钟
     */
    private Long duration;

    /**
     * 持续时间，单位分钟
     */
    private Long idealDuration;


    /**
     * 打开次数
     */
    private Integer openNum;

    /**
     * 首次封单量
     */
    private Long firstVol;

    private Long highestVol;


    /**
     * 有效的封单量
     */
    private Long firstValidVol;

    private Long highestValidVol;

    /**
     * 强弱描述
     */
    private String strongWeakDescribe;

}
