package com.coatardbul.stock.model.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockTradeBuyConfig {
    private String id;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 买入金额
     */
    private BigDecimal allMoney;

    /**
     * 剩余仓位金额
     */
    private BigDecimal subMoney;

    /**
     * 可以买入的次数
     */
    private Integer allNum;

    /**
     * 剩余次数
     */
    private Integer subNum;

    /**
     * 仓位占比
     */
    private BigDecimal proportion;

    /**
     * 1.直接买入
     * 2.邮件提醒
     * 3.固定策略
     */
    private Integer type;
}