package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
 * 自选板块
 */
@Data
public class StockOptionalPlate {
    private String id;

    /**
     * 板块名称
     */
    private String name;

    /**
     * 板块标识
     */
    private String plateSign;

    private String remark;
}