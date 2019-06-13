package com.azkaban.config;


import com.azkaban.api.AzkabanApi;
import com.azkaban.proxy.AzkabanApiProxyBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by shirukai on 2019-06-03 11:05
 * 配置API，创建Bean，并注入Spring
 */
@Configuration
public class AzkabanApiConfig {
    @Value("${azkaban.url}")
    private String uri;

    @Value("${azkaban.username}")
    private String username;

    @Value("${azkaban.password}")
    private String password;

    @Bean
    public AzkabanApi azkabanApi() {
        return AzkabanApiProxyBuilder.create()
                .setUri(uri)
                .setUsername(username)
                .setPassword(password)
                .build();
    }
}
