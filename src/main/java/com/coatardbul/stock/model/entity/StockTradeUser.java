package com.coatardbul.stock.model.entity;

import java.util.Date;
import lombok.Data;

@Data
public class StockTradeUser {
    private String id;

    /**
    * 账号
    */
    private String account;

    /**
    * 密码
    */
    private String password;

    /**
    * 过期时间
    */
    private Date expireTime;

    private String cookie;
}