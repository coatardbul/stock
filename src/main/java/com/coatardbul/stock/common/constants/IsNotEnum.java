package com.coatardbul.stock.common.constants;

public enum IsNotEnum {
    STOCK(1,"是"),



    CONCEPT(0,"否");

    private Integer type;

    private String desc;

    IsNotEnum(Integer type, String desc) {
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
