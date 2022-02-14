package com.coatardbul.stock.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
    *
    */
@Data
public class StockEmotionQueryDTO {
    /**
     * YYYY-MM-DD
     */
    private String dateStr;


    /**
     * 对象枚举标识
     */
    private String objectEnumSign;

    /**
     * 时间间隔
     */
    private Integer timeInterval;

}