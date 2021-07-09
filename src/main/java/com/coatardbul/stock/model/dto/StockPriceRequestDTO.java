package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2021/7/8
 *
 * @author Su Xiaolei
 */
@Data
public class StockPriceRequestDTO {

    private String code;


    private String beginDate;
    
    private String endDate;
}
