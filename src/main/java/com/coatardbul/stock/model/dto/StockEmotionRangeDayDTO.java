package com.coatardbul.stock.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
    *
    */
@Data
public class StockEmotionRangeDayDTO {
    /**
     * YYYY-MM-DD
     */
    private String dateBeginStr;

    /**
     * YYYY-MM-DD
     */
    private String dateEndStr;

    /**
     * 对象枚举标识
     */
    @NotBlank(message = "对象标识不能为空")
    private String objectEnumSign;



}