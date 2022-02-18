package com.coatardbul.stock.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
    *
    */
@Data
public class StockEmotionDayDTO {
    /**
     * YYYY-MM-DD
     */
    @NotBlank(message = "YYYY-MM-DD不能为空")
    private String dateStr;


    /**
     * 对象枚举标识
     */
    @NotBlank(message = "对象标识不能为空")
    private String objectEnumSign;

    /**
     * 时间间隔
     */
//    @NotNull(message = "时间间隔不能为空")
    private Integer timeInterval;


    /**
     * 目前仅限于定时任务使用
     * HH:mm
     */
    private String timeStr;

}