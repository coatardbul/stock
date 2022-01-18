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
public class StockExcelStaticQueryDTO extends  StockStrategyQueryBaseDTO{



    private String dateBeginStr;


    private String dateEndStr;


}
