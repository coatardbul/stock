package com.coatardbul.stock.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 两板以上涨停名称信息
 */
@Data
public class StockUpLimitNameBO {

    private String upLimitNum;

    private List<String> nameList;

}