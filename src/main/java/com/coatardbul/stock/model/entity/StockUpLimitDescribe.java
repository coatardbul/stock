package com.coatardbul.stock.model.entity;

import lombok.Data;

@Data
public class StockUpLimitDescribe {
    private String id;

    private String date;

    private String code;

    private String name;

    private String upLimitType;

    private String upLimitInfo;
}