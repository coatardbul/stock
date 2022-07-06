package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockUplimitAnalyze {
    private String id;

    private String date;

    private String objectSign;

    private String jsonDetail;

    private String code;

    /**
     * 昨日换手率
     */
    private String lastTurnOver;

    /**
     * 昨日量比
     */
    private String lastVol;

    /**
     * 昨日承压
     */
    private String compressionDivision;

    /**
     * 竞价换手率
     */
    private String auctionTurnOver;

    /**
     * 竞价涨幅
     */
    private String auctionIncrease;

    /**
     * 最新涨幅
     */
    private String newIncrease;

    /**
     * 流通市值
     */
    private String circulationMarketValue;

    /**
     * 目前价格
     */
    private String currentPrice;

    /**
     * 竞价量比
     */
    private String auctionVol;

    /**
     * 市值
     */
    private String marketValue;
}