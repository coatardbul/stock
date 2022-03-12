package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
 * 股票情绪
 */
@Data
public class StockMinuterEmotion {
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
     * 模板id
     */
    private String templateId;

    /**
     * 如果是组合计算，需要标注名称
     */
    private String name;
}