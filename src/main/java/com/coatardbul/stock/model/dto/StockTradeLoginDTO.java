package com.coatardbul.stock.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/3
 *
 * @author Su Xiaolei
 */
@Data
public class StockTradeLoginDTO {

    private String id;

    private String userId;

    private String password;

    private String randNumber;

    private String identifyCode;

    private String duration;

    private String authCode;

    private String type;


}
