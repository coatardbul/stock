package com.coatardbul.stock.model.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockFinance {
    /**
     * code
     */
    private String code;

    /**
     * 日期
     */
    private String date;

    /**
     * 融资买入（元）
     */
    private BigDecimal name1;

    /**
     * 融资净买入（元）
     */
    private BigDecimal name2;

    /**
     * 融资融券余额（元）
     */
    private BigDecimal name3;
}