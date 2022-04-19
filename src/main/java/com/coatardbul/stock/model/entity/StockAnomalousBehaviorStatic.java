package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
    * 股票异常行为概览表
    */
@Data
public class StockAnomalousBehaviorStatic {
    private String id;

    private String code;

    /**
    * 股票名称
    */
    private String name;

    /**
    * 统计开始时间
    */
    private String beginDate;

    /**
    * 统计结束时间
    */
    private String endDate;

    /**
    * 统计详情
    */
    private String staticDetail;
}