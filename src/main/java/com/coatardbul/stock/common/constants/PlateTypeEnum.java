package com.coatardbul.stock.common.constants;

public enum PlateTypeEnum {
    STOCK(1,"股票"),



    CONCEPT(2,"概念"),


    INDUSTRY(3,"行业"),

    TERRITORY(4,"地域");

    private Integer type;

    private String desc;

    PlateTypeEnum(Integer type, String desc) {
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
