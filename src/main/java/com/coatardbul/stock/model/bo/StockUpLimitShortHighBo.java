package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/11
 *
 * @author Su Xiaolei
 */
@Data
public class StockUpLimitShortHighBo {


    private Integer isFilterStatus;


    private BigDecimal rate;
}
