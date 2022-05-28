package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
 * 股票过滤
 */
@Data
public class StockFilter {
    private String id;

    /**
     * 过滤的日期
     */
    private String date;

    /**
     * 问句模板标识
     */
    private String templateSign;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 智能描述
     */
    private String explain;

    /**
     * 状态：0默认状态 1.标记状态（即重点管住）
     */
    private Integer status;
}