package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class ModulePrice {
    /**
    * 股票编码
    */
    private String code;

    /**
    * 日期
    */
    private String date;

    /**
    * 股票名称
    */
    private String name;

    /**
    * 开盘价
    */
    private Long openPrice;

    /**
    * 收盘价
    */
    private Long closePrice;

    /**
    * 最低价
    */
    private Long minPrice;

    /**
    * 最高价
    */
    private Long maxPrice;

    /**
    * 平开价（即昨日收盘价）
    */
    private Long lastClosePrice;

    /**
    * 换手率
    */
    private Long turnOverRate;

    /**
    * 量比
    */
    private Long quantityRelativeRatio;

    /**
    * 量
    */
    private Integer volumn;
}