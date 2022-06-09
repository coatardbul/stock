package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/5
 *
 * @author Su Xiaolei
 */
@Data
public class StockUserCookieDTO {

    private String id;

    private Integer duration;


    private String cookie;

    private String validatekey;
}
