package com.coatardbul.stock.model.dto;

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
public class StockStrategyQueryDTO {

    private String templateId;

    private String dateStr;
    /**
     * 查询字符串
     */
    private String queryStr;


    private String cookie;

    /**
     * 页码相关
     */
    private Integer pageSize;

    private Integer page;

    /**
     * 排序相关
     */
    private String orderStr;

    private String orderBy;


}
