package com.coatardbul.stock.common.constants;

public enum StockWatchTypeEnum {

    POSITION(1,"持仓股票"),
    CRON_WATCH(2,"定时任务策略扫描"),
    EMAIL(3,"需要发送邮件"),
    BUY_SELL(4,"买入，卖出"),
    STRATEGY_SIMULATE(5,"策略模拟");
    private Integer type;

    private String desc;

    StockWatchTypeEnum(Integer type, String desc) {
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
