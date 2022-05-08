package com.coatardbul.stock.model.feign;

import lombok.Data;

/**
    * 股票问句模板
    */
@Data
public class StockTemplateQueryDTO {
    private String id;
    /**
     * HH:mm
     */
    private String  timeStr;


    /**
     * 题材
     */
    private String themeStr;

    private String objectSign;

    private String stockCode;
    /**
    * 模板名称
    */
    private String name;

    /**
     * 当前问句查询日期YYYY-MM-DD
     */
    private String dateStr;


    /**
     * 股票脚本
     */
    private String stockScript;

}