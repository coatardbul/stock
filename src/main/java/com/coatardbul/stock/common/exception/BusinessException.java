package com.coatardbul.stock.common.exception;

public class BusinessException extends RuntimeException{

    public BusinessException(String str) {
        super(str);
    }

    public BusinessException(String code,Exception e){
        super(code,e);
    }
}
