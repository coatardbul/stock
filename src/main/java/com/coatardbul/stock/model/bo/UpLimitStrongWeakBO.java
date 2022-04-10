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
     * 涨停时间
     */
    private Date firstUpLimitDate;

    /**
     * 持续时间，单位分钟
     */
    private Long duration;


    private Integer openNum;

    private Long firstVol;

    private Long highestVol;

    private String strongWeakDescribe;

}
