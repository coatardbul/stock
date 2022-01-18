package com.coatardbul.stock.common.constants;

public enum CookieEnum {


    strategy("strategy","同花顺问财");

    private String code;

    private String desc;


    CookieEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
