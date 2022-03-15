package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/14
 *
 * @author Su Xiaolei
 */
@Data
public class ProxyIpQueryDTO {

    private String orderId;

    private Integer num;


    private String format;


    private String lineSeparator;


    private String canRepeat;

    private String userToken;

}
