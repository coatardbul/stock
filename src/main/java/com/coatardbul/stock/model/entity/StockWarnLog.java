package com.coatardbul.stock.model.entity;

import java.util.Date;
import lombok.Data;

/**
    * 预警日志表
    */
@Data
public class StockWarnLog {
    private String id;

    /**
    * 股票代码
    */
    private String stockCode;

    /**
    * 股票名称
    */
    private String stockName;

    /**
    * 模板id
    */
    private String templateId;

    /**
    * 模板名称
    */
    private String templateName;

    /**
    * 创建日期
    */
    private String date;

    private Date createTime;
}