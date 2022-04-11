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
public class UpLimitStrongWeakBO {

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


    private Integer openNum;

    private Long firstVol;

    private Long highestVol;

    private String strongWeakDescribe;

}
