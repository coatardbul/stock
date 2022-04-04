package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/4
 *
 * @author Su Xiaolei
 */
@Data
public class StockPredictDto {

    private String id;


    /**
     * 买入时间HH:mm
     */
    private String buyTime;


    private String beginDate;


    private String endDate;

    /**
     * 持有天数
     */
    private Integer holeDay;

    /**
     * 卖出时间HH:mm
     */
    private String saleTime;
}
