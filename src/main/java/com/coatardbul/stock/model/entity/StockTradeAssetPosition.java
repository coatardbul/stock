package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
    * 股票持仓
    */
@Data
public class StockTradeAssetPosition {
    private String id;

    private String name;

    private String code;

    /**
    * 总持仓
    */
    private String allPosition;

    /**
    * 目前持仓
    */
    private String currPosition;

    /**
    * 时间
    */
    private String date;

    /**
    * 持仓类型：1.总账户 2 单个股票持仓
    */
    private Integer type;
}