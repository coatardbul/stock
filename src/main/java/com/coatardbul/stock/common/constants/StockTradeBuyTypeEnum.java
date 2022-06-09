package com.coatardbul.stock.common.constants;

public enum StockTradeBuyTypeEnum {

    DIRECT(1,"直接买入"),
    EMAIL(2,"需要发送邮件"),
    FIX_STRATEGY(3,"固定策略");
    private Integer type;

    private String desc;

    StockTradeBuyTypeEnum(Integer type, String desc) {
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
