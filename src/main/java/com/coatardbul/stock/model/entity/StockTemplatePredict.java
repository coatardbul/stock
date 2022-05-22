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

    /**
     * 模板id
     */
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
     * 卖出价格
     */
    private BigDecimal salePrice;

    /**
     * 具体详情
     */
    private String detail;

    /**
     * 买入时间 年月日时分
     */
    private String buyTime;

    /**
     * 换手率
     */
    private BigDecimal turnoverRate;
}