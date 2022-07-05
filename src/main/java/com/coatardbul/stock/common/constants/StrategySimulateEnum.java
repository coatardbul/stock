package com.coatardbul.stock.common.constants;

public enum StrategySimulateEnum {
    UPLIMIT_SHORT_HIGH("uplimit_short_high","首板涨停时间短，二板还能高开"),
    BUY_SELL("buy_sell","买卖");

    private String sign;

    private String desc;

    StrategySimulateEnum(String sign, String desc) {
        this.sign = sign;
        this.desc = desc;
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
}
