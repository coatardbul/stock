package com.coatardbul.stock.model.entity;

import lombok.Data;

/**
 * 股票统计模板
 */
@Data
public class StockStaticTemplate {
    private String id;

    /**
     * 统计纬度;按照天统计，按照分钟统计
     */
    private String staticLatitude;

    /**
     * 描述：作用和实现方式，统计是否叠加
     */
    private String remark;

    /**
     * 排序字段，有的时候需要排序，按照数组惊醒排序
     */
    private String orderBy;

    /**
     * 存储对象，可能是对象，可能是全限定名称
     */
    private String objectSign;

    /**
     * 对象体
     */
    private String objectStr;
}