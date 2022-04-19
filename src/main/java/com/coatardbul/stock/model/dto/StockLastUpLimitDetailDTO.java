package com.coatardbul.stock.model.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockLastUpLimitDetailDTO {



    private String riverStockTemplateId;

    /**
     * YYYY-MM-DD
     */
    private String dateStr;

    /**
     * YYYY-MM-DD
     */
    private String beginDateStr;

    /**
     * YYYY-MM-DD
     */
    private String endDateStr;

    private List<String> plateList;

    /**
     *股票代码
     */
    private String stockCode;


}
