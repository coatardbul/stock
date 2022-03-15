package com.coatardbul.stock.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Random;

@Aspect
@Component
@Slf4j
public class CallBackLogInterceptor {



    @Before("execution(* com.coatardbul.stock.service.base.StockStrategyService.strategy(..))")
    public void strategyWait(){
//        try {
//            int i = new Random().nextInt(9999) + 1000;
//            Thread.sleep(i);
//        } catch (Exception e) {
//            log.error(e.getMessage(),e);
//        }
    }

}