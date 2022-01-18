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
public class StockStrategyQueryBaseDTO {

    private String excelTemplateId;


    /**
     * 页码相关
     */
    private Integer pageSize;

    private Integer page;

}
