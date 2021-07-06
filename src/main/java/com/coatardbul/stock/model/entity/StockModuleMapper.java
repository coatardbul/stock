package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockModuleMapper {
    /**
    * 股票代码
    */
    private String stockCode;

    /**
    * 板块代码
    */
    private String moduleCode;
}