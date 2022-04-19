package com.coatardbul.stock.common.constants;

public enum PlateEnum {
    AM_OPEN_LOW_LIMIT_ONE("AM_OPEN_LOW_LIMIT_ONE","开盘一板数据","1516041839138963456"),
    AM_OPEN_LOW_LIMIT_TWO("AM_OPEN_LOW_LIMIT_TWO","开盘二板数据","1516067414108930048");

    private String sign;

    private String desc;

    private String id;

    PlateEnum(String sign, String desc, String id) {
        this.sign = sign;
        this.desc = desc;
        this.id = id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
