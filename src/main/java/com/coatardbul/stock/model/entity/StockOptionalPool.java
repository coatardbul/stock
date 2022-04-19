package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockOptionalPool {
    private String id;

    private String code;

    private String name;

    /**
     * 板块id
     */
    private String plateId;

    private String plateName;
}