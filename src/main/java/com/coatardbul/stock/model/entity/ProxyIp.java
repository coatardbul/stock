package com.coatardbul.stock.model.entity;

import java.util.Date;
import lombok.Data;

@Data
public class ProxyIp {
    private String id;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private String port;

    /**
     * 国家
     */
    private String country;

    /**
     * 省
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    private Date createTime;

    /**
     * 最多8次
     */
    private Integer useTime;
}