package com.coatardbul.stock.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

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
    @NotBlank(message = " 对象标识不能为空")
    private String objectEnumSign;

}