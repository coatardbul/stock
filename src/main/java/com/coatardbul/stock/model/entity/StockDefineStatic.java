package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
    * 自定义统计
    */
@Data
public class StockDefineStatic {
    private String id;

    /**
    * 日期
    */
    private String date;

    private String name;

    /**
    * 中位数
    */
    private String median;

    /**
    * 标准差
    */
    private String std;

    /**
    * 对象标识
    */
    private String objectSign;
}