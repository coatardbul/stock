package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockStaticQueryDTO extends  StockStrategyQueryBaseDTO{

    private String dateStr;



    private String timeStr;
}
