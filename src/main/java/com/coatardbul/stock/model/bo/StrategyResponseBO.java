package com.coatardbul.stock.model.bo;

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
public class StrategyResponseBO {

    private Integer status_code;


    private String status_msg;

    private String logid;


    private Object data;

}
