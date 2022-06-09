package com.coatardbul.stock.common.constants;

public enum StockTradeSellTypeEnum {
    TIME_SELL(1,"定时卖出，需要卖出时间"),
    CONDITION_SELL(2,"条件卖出"),
    EMAIL(3,"邮件提醒");
    private Integer type;

    private String desc;

    StockTradeSellTypeEnum(Integer type, String desc) {
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
