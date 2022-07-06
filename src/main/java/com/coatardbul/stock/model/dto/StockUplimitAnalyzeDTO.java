package com.coatardbul.stock.model.dto;

import lombok.Data;

@Data
public class StockUplimitAnalyzeDTO {


    private String beginDateStr;

    private String endDateStr;

    private String objectSign;


    /**
     * 昨日换手率
     */
    private String beginLastTurnOver;

    /**
     * 昨日换手率
     */
    private String endLastTurnOver;
    /**
     * 昨日量比
     */
    private String beginLastVol;
    /**
     * 昨日量比
     */
    private String endLastVol;
    /**
     * 昨日承压
     */
    private String beginCompressionDivision;
    /**
     * 昨日承压
     */
    private String endCompressionDivision;

    /**
     * 竞价涨幅
     */
    private String beginAuctionIncrease;

    /**
     * 竞价涨幅
     */
    private String endAuctionIncrease;
    /**
     * 最新涨幅
     */
    private String beginNewIncrease;

    /**
     * 最新涨幅
     */
    private String endNewIncrease;
    /**
     * 竞价换手率
     */
    private String beginAuctionTurnOver;

    /**
     * 竞价换手率
     */
    private String endAuctionTurnOver;

    /**
     * 竞价量比
     */
    private String beginAuctionVol;

    /**
     * 竞价量比
     */
    private String endAuctionVol;

    /**
     * 市值
     */
    private String beginMarketValue;
    /**
     * 市值
     */
    private String endMarketValue;
    /**
     * 流通市值
     */
    private String beginCirculationMarketValue;

    /**
     * 流通市值
     */
    private String endCirculationMarketValue;
}