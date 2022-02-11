package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Note:  轴线基础数据
 * <p>
 * Date: 2022/2/11
 *
 * @author Su Xiaolei
 */
@Data
public class AxiosBaseBo {

    private String dateTimeStr;

    private BigDecimal value;
}
