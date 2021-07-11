package com.coatardbul.stock.model.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockBase {
    /**
     * 股票代码
     */
    private String code;

    /**
     * 股票名称
     */
    private String name;

    /**
     * 流通股本
     */
    private BigDecimal circulatingStockCapital;

    /**
     * 流通市值
     */
    private BigDecimal circulatingStockValue;

    /**
     * 总股本
     */
    private BigDecimal allStockCapital;

    /**
     * 总市值
     */
    private BigDecimal allStockValue;

    /**
     * 市盈率
     */
    private BigDecimal per;

    /**
     * 市净率
     */
    private BigDecimal pbr;
}