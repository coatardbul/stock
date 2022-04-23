package com.coatardbul.stock.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 两板以上涨停名称信息
 */
@Data
public class StockUpLimitNameBO {

    /**
     * 涨停板块
     */
    private String upLimitNum;

    /**
     * 名称List
     */
    private List<String> nameList;

}