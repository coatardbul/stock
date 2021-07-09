package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Note: 同花顺对象
 * <p>
 * Date: 2021/7/7
 *
 * @author Su Xiaolei
 */
@Data
public class ThsPriceBO {


    /**
     * 总数
     */
    private  String total;

    /**
     * 开始时间yyyyMMdd
     */
    private String start;

    /**
     * 股票名称
     */
    private String name;


    private String marketType;
    /**
     * 每一年包括的天数
     */
    private List<List<Integer>> sortYear;

    private Integer priceFactor;
    /**
     * 最低价格，？，？，？
     */
    private String price;

    /**
     * 成交量
     */
    private String volumn;

    private  String afterVolumn;


    /**
     * 日期 MMdd
     */
    private String dates;

    private String issuePrice;
}
