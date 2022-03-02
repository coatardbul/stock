package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:支持两种模式
 * 1.传入id，日期，时间
 * 2.直接传入问句
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockStrategyQueryDTO {

    private String riverStockTemplateId;

    /**
     * YYYY-MM-DD
     */
    private String dateStr;

    /**
     * HH:MM
     */
    private String timeStr;

    /**
     *股票代码
     */
    private String stockCode;
    /**
     * 查询字符串
     */
    private String queryStr;

    /**
     * 页码相关
     */
    private Integer pageSize;

    private Integer page;

    /**
     * 排序相关
     */
    private String orderStr;

    private String orderBy;


}
