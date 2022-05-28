package com.coatardbul.stock.common.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="system.proxy")
@Data
public class ProxyConfig {
    /**
     * 是否启用代理
     */
    private Boolean enabled;
    /**
     * 代理主机地址
     */
    private String host;
    /**
     * 代理端口
     */
    private Integer port;
}