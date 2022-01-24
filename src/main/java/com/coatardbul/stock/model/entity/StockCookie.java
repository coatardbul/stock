package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockCookie {
    private String id;

    /**
     * 英文描述key
     */
    private String typeKey;

    private String cookieValue;

    /**
     * 描述
     */
    private String remark;
}