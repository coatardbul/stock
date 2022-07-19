package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
    * 交易策略
    */
@Data
public class StockTradeStrategy {
    private String id;

    /**
    * 名称
    */
    private String name;

    /**
    * 策略标识，唯一，与代码中的枚举一一对应
    */
    private String sign;

    /**
    * 表达式样例
    */
    private String expressExample;

    /**
    * 默认参数
    */
    private String defaultParam;

    /**
    * 表达脚本
    */
    private String expressScript;

    /**
    * 标识:买入B，卖出S
    */
    private String type;

    /**
    * 定时任务的类的全限定路径
    */
    private String jobClass;
}