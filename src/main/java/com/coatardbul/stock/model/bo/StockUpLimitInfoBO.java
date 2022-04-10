package com.coatardbul.stock.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
    *
    */
@Data
public class StockUpLimitInfoBO {

    private String upLimitNum;

    private List<String> nameList;

    private Integer num;


}