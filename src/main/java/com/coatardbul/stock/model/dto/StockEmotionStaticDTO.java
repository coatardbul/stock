package com.coatardbul.stock.model.dto;

import lombok.Data;

/**
 * 股票情绪
 */
@Data
public class StockEmotionStaticDTO {


    private String date;



    /**
     * 根据对象解析对应的数据，保留字段
     */
    private String objectSign;

    /**
     * 时间间隔，单位分钟
     */
    private Integer timeInterval;

    /**
     * 时间间隔，单位分钟
     */
    private Integer timeIntervalCount;

}