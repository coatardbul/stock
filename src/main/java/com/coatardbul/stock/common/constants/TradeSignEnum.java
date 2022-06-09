package com.coatardbul.stock.common.constants;

public enum TradeSignEnum {
    ASSET_POSITION("asset_position","持仓"),



    BUY_SELL("buy_sell","买卖");

    private String sign;

    private String desc;

    TradeSignEnum(String sign, String desc) {
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
