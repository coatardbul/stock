package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
    * 自定义统计
    */
@Data
public class StockDefineStaticDTO {

    private String beginDateStr;

    private String endDateStr;

    /**
    * 对象标识
    */
    private String objectSign;
}