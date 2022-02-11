package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
    * 股票统计模板
    */
@Data
public class StockStaticTemplateBaseDTO {
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
     * 对象枚举标识
     */
    private String objectEnumSign;

    /**
    * 对象体
    */
    private String objectJson;

    /**
     * 时间间隔，对于天，默认为1，分钟需要设置间隔时间
     */
    private Integer timeInterval;
}