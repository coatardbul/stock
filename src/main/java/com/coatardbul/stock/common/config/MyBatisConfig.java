package com.coatardbul.stock.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.coatardbul.stock.mapper")
public class MyBatisConfig {
}
