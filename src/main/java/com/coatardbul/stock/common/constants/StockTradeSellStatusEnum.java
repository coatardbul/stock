package com.coatardbul.stock.common.constants;

public enum StockTradeSellStatusEnum {
    NO_SELL(1,"未卖出"),
    HAVE_SELL(2,"已卖出");
    private Integer type;

    private String desc;

    StockTradeSellStatusEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
