package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
    *
    */
@Data
public class StockEmotionDayRangeDTO {
    /**
     * YYYY-MM-DD
     */
    private String beginDate;

    /**
     * YYYY-MM-DD
     */
    private String endDate;


    /**
     * 对象枚举标识
     */
    private String objectEnumSign;

}