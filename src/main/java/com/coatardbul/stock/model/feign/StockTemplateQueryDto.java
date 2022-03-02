package com.coatardbul.stock.model.feign;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
    * 股票问句模板
    */
@Data
public class StockTemplateQueryDto {
    @NotBlank(message = "id不能为空")
    private String id;

    /**
    * 模板名称
    */
    private String name;

    /**
     * 当前问句查询日期YYYY-MM-DD
     */
    @NotBlank(message = "日期不能为空")
    private String dateStr;
    /**
     * HH:mm
     */
    private String  timeStr;
    private String stockCode;
}