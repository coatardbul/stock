package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockExcelTemplate {
    private String id;

    /**
     * 上涨id
     */
    private String riseId;

    /**
     * 谢跌id
     */
    private String failId;

    /**
     * 昨日涨停
     */
    private String limitUpId;

    private String limitUpOneId;

    private String limitUpTwoId;

    private String limitUpThreeId;

    private String remark;

    /**
     * 字符串匹配
     */
    private String orderStr;

    /**
     * 排序字段
     */
    private String orderBy;
}