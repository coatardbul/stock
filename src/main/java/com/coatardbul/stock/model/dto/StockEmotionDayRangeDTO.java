package com.coatardbul.stock.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
    *
    */
@Data
public class StockEmotionDayRangeDTO {
    /**
     * YYYY-MM-DD
     */
    @NotBlank(message = "开始YYYY-MM-DD不能为空")
    private String beginDate;

    /**
     * YYYY-MM-DD
     */
    @NotBlank(message = "结束YYYY-MM-DD不能为空")
    private String endDate;


    /**
     * 对象枚举标识
     */
    @NotBlank(message = "对象枚举标识不能为空")
    private String objectEnumSign;

    /**
     * 时间间隔
     */
//    @NotNull(message = "时间间隔不能为空")
    private Integer TimeInterval;

}