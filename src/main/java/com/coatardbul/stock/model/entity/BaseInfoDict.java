package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class BaseInfoDict {
    /**
     * 股票编码
     */
    private String code;

    /**
     * 股票名称
     */
    private String name;

    /**
     * 路径
     */
    private String url;

    /**
     * 1 股票 2 概念 3 行业
     */
    private Integer type;
}