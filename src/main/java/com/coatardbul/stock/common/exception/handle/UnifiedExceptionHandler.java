package com.coatardbul.stock.common.exception.handle;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;

@Component
@Slf4j
@ControllerAdvice
public class UnifiedExceptionHandler {
    /**
     * 绑定的异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public String constraintViolation(ConstraintViolationException e) {
        log.info(e.getMessage(),e);
        return "11111";
    }

    @ExceptionHandler({BusinessException.class})
    @ResponseBody
    public CommonResult business(BusinessException e) {
        return CommonResult.failed(e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    @ResponseBody
    public CommonResult fjlk12321sdj(Exception e) {
        log.info(e.getMessage(),e);
        return CommonResult.failed(e.getMessage());
    }


    //TODO 断言的定义，业务类的异常的定义
    //TODO controller 上的异常
}
