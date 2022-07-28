package com.coatardbul.stock.model.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/18
 *
 * @author Su Xiaolei
 */
@Data
public class StockOptionalPoolQueryDTO  {

    private String name;

    private List<String> plateList;
}
