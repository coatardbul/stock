package com.coatardbul.stock.common.interceptor;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
//@Intercepts(value = {
//        @Signature(type = Executor.class,
//                method = "update",
//                args = {MappedStatement.class, Object.class}),
//        @Signature(type = Executor.class,
//                method = "query",
//                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
//                        CacheKey.class, BoundSql.class}),
//        @Signature(type = Executor.class,
//                method = "query",
//                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
@Slf4j
@Component
public class MybatisInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("###########################进入了日志拦截器##################");
        //得到拦截的对象，其实就是StatementHandler
        Object target = invocation.getTarget();
        StatementHandler statementHandler = (StatementHandler) target;
        try {
            return invocation.proceed();
        } finally {

            try{
                //获取绑定的SQL对象
                BoundSql boundSql = statementHandler.getBoundSql();
                //得到需要执行的sql语句，并进行格式
                String sql = boundSql.getSql();
                if(sql.startsWith("insert into xt_rz_sql(")){//此时是插入的sql日志内容，不用去处理
                    //什么都不用做
                }else{
                    //得到默认的参数处理器
                    DefaultParameterHandler dph=(DefaultParameterHandler)statementHandler.getParameterHandler();
                    //利用反射机制，从DefaultParameterHandler获取Configuration和TypeHandlerRegistry
                    Field configurationField=dph.getClass().getDeclaredField("configuration");
                    Field typeHandlerRegistryField=dph.getClass().getDeclaredField("typeHandlerRegistry");
                    configurationField.setAccessible(true);//设置私有属性可访问
                    typeHandlerRegistryField.setAccessible(true);//设置私有属性可访问
                    Configuration configuration=(Configuration) configurationField.get(dph);
                    TypeHandlerRegistry typeHandlerRegistry=(TypeHandlerRegistry) typeHandlerRegistryField.get(dph);
                    //sql的参数对象
                    Object parameterObject = boundSql.getParameterObject();
                    //需要绑定的参数映射对象
                    List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
                    //处理sql的参数，该部分参考的是DefaultParameterHandler中setParameters方法中的实现
                    StringBuffer args=new StringBuffer();
                    if(parameterMappingList!=null && parameterMappingList.size()>0){
                        for(ParameterMapping parameterMapping:parameterMappingList){
                            //如果该参数不是输出参数，则进行处理
                            if (parameterMapping.getMode() != ParameterMode.OUT) {
                                Object value;
                                //参数的名字，属性
                                String propertyName = parameterMapping.getProperty();
                                //先从附加的，主要是list、array等的处理
                                if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                                    value = boundSql.getAdditionalParameter(propertyName);
                                } else if (parameterObject == null) {
                                    value = null;
                                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                                    //typeHandlerRegistry注册了某个类的处理
                                    value = parameterObject;
                                } else {
                                    //默认的MetaObject 的处理，根据参数获取值
                                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                                    value = metaObject.getValue(propertyName);
                                }

                                args.append(",").append(value);
                            }
                        }
                        args.deleteCharAt(0);//删除第一个逗号
                    }


                }
            }catch(Exception e){
                log.error("记录SQL日志时出现异常："+e.getMessage(),e);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        if(target instanceof StatementHandler){
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
