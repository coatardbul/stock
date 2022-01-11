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
public class StockStaticQueryDTO {

    /**
     * 上涨家数
     */
    private String riseId;
    /**
     * 下跌家数
     */
    private String failId;
    /**
     * 昨日涨停
     */
    private String limitUpId;


    private String dateStr;


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

    /**
     * 计算数据key
     */
    private String keyStr;
}
