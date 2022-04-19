package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.util.List;

/**
 * 首板涨停信息
 */
@Data
public class StockUpLimitInfoBO {
    private String themeName;

    private List<LimitBaseInfoBO> nameList;

    private Integer num;


}