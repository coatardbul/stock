package com.coatardbul.stock.model.feign;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
    * 股票问句模板
    */
@Data
public class StockTemplateDto {
    private String id;

    /**
    * 模板名称
    */
    @NotBlank(message = "模板名称不能为空")
    private String name;

    /**
    * 模板表达式
    */
    private String scriptStr;

    /**
    * 表达式样例
    */
    @NotBlank(message = "表达式样例不能为空")
    private String exampleStr;
}