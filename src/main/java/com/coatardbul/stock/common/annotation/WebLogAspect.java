package com.coatardbul.stock.common.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 将请求的参数
 */
@Component
@Aspect
@Slf4j
public class WebLogAspect {
    @Pointcut(value = "@within(com.coatardbul.stock.common.annotation.WebLog)")
    private void pointCut() {
    }

    //    @Before(value = "pointCut() && @annotation(logger)")
//    public void before(JoinPoint joinPoint, KtLog logger){
//        System.out.println("注解作用的方法名: " + joinPoint.getSignature().getName());
//
//        System.out.println("所在类的简单类名: " + joinPoint.getSignature().getDeclaringType().getSimpleName());
//
//        System.out.println("所在类的完整类名: " + joinPoint.getSignature().getDeclaringType());
//
//        System.out.println("目标方法的声明类型: " + Modifier.toString(joinPoint.getSignature().getModifiers()));
//
//    }
    @Around("pointCut()")
    public Object advice(ProceedingJoinPoint joinPoint) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.info("--------" +request.getMethod()+ request.getRequestURI() + "----开始");
        log.info("--------" +request.getQueryString() + "----开始");
        Map map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    System.out.println("参数：" + paramName + "=" + paramValue);
                    map.put(paramName, paramValue);
                }
            }
        }
//        HttpServletRequest request = (HttpServletRequest) RequestContextHolder.getRequestAttributes().resolveReference("request");
//        try {
//            log.info("-------------------------------------\t\tController start:{}\t\t------------------------------------------------", request.getRequestURI());
//            log.info("Request: [method: \"{}\", params: \"{}\" , body: \"{}\" ]", new Object[]{request.getMethod(), request.getQueryString(), HttpHelper.getBodyString(request)});
//        } catch (Exception var4) {
//            log.error(var4.getMessage());
//        }
//
//        log.info("x-token : " + request.getHeader("x-token"));
//        Object result = joinPoint.proceed();
//        log.info("response:\t[{}]", JSONUtil.toJsonStr(result));
//        log.info("--------------------------------------\t\tcontroller end:{}\t\t-------------------------------------------------", request.getRequestURI());

//        System.out.println("["
//                + joinPoint.getSignature().getDeclaringType().getSimpleName()
//                + "][" + joinPoint.getSignature().getName()
//                + "]-日志内容-[" + logger.value() + "]");
        Object result = null;

        Object[] args = joinPoint.getArgs();
        try {
            result = joinPoint.proceed(args);
        } catch (Throwable throwable) {
           log.error(throwable.getMessage(),throwable);
        }
        log.info("--------" + request.getRequestURI() + "----结束");
        return result;

    }
}
