package com.coatardbul.stock.common.constants;

public enum StaticLatitudeEnum {


    minuter("1","分钟",true),
    day("2","天",false);

    private String code;

    private String desc;

    private Boolean flag;

    StaticLatitudeEnum(String code, String desc, Boolean flag) {
        this.code = code;
        this.desc = desc;
        this.flag = flag;
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

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    /**
     * code是否在存在枚举中
     * @param code
     * @return
     */
    public static Boolean isHaveCode(String code){
        for(StaticLatitudeEnum staticLatitude:StaticLatitudeEnum.values()){
            if(code.equals(staticLatitude.getCode())){
                return true;
            }
        }
        return false;
    }

    /**
     * code对应的传值能否为空
     * @param code
     * @param interval
     * @return
     */
    public static Boolean isNotBlankVerify(String code,Integer interval ){
        for(StaticLatitudeEnum staticLatitude:StaticLatitudeEnum.values()){
            //code相同
            if(code.equals(staticLatitude.getCode())){
                //需要验证，对应的间隔不能为空
                if(staticLatitude.flag){
                    return interval!=null;
                }else {
                    //不需要验证，直接返回
                    return true;
                }
            }
        }
        return false;
    }
}
