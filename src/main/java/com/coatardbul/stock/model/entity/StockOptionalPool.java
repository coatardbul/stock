package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockOptionalPool {
    private String id;

    private String code;

    private String name;

    /**
    * 1.买入的股票
2.监控的股票
3.潜力股票
    */
    private Integer type;
}