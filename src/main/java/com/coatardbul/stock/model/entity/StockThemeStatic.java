package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
    * 股票题材统计
    */
@Data
public class StockThemeStatic {
    private String id;

    private String date;

    /**
    * 题材
    */
    private String theme;

    /**
    * 对象
    */
    private String objectStaticArray;

    /**
    * 对象标识
    */
    private String objectSign;
}