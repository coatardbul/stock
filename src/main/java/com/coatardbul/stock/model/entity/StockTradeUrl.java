package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockTradeUrl {
    private String id;

    /**
     * 路径
     */
    private String url;

    /**
     * 标识
     */
    private String sign;

    /**
     * 描述
     */
    private String remark;

    private String validateKey;
}