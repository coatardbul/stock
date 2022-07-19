package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
    * 交易策略
    */
@Data
public class StockTradeStrategyDTO {

    /**
    * 名称
    */
    private String name;


    /**
    * 表达式样例
    */
    private String expressExample;


    /**
    * 标识:买入B，卖出S
    */
    private String type;

}