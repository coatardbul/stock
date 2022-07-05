package com.coatardbul.stock.model.entity;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 股票模型预测表
 */
@Data
public class StockTemplatePredict {
    private String id;

    /**
     * YYYY-MM-DD日期
     */
    private String date;

    /**
     * 模板id
     */
    private String templatedId;
    private String templatedName;

    /**
     * 持有天数
     */
    private Integer holdDay;

    /**
     * 卖出时间，年月日时分
     */
    private String saleTime;

    /**
     * 股票代码
     */
    private String code;

    private String name;

    /**
     * 市值
     */
    private BigDecimal marketValue;

    /**
     * 买入价格
     */
    private BigDecimal buyPrice;

    /**
     * 买入涨幅比例
     */
    private BigDecimal buyIncreaseRate;

    /**
     * 收盘涨幅
     */
    private BigDecimal closeIncreaseRate;

    /**
     * 具体详情
     */
    private String detail;

    /**
     * 换手率
     */
    private BigDecimal turnoverRate;

    /**
     * 卖出价格
     */
    private BigDecimal salePrice;

    /**
     * 买入时间 年月日时分
     */
    private String buyTime;
}