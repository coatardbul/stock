package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/5/8
 *
 * @author Su Xiaolei
 */
@Data
public class StockFilterDeleteInfoDTO {


    private String date;

    /**
     * 模板标识
     */
    private String templateSign;

    /**
     * 股票代码
     */
    private String stockCode;






}
