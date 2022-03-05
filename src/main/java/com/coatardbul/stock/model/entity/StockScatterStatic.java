package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
    * 股票散点统计
    */
@Data
public class StockScatterStatic {
    /**
    * 主键
    */
    private String id;

    private String date;

    /**
    * 对象集合
    */
    private String objectStaticArray;

    /**
    * 根据对象解析对应的数据，保留字段
    */
    private String objectSign;

    /**
    * 对象说明
    */
    private String remark;
}