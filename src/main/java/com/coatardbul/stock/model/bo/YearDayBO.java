package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2021/7/8
 *
 * @author Su Xiaolei
 */
@Data
public class YearDayBO {

    /**
     * yyyy
     */
    private Integer year;

    /**
     * 一年中的交易日
     */
    private Integer dayOfYear;
}
