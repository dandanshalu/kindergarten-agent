package com.kindergarten.config;

import com.kindergarten.service.LlmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * LLM 相关配置。
 *
 * EnableConfigurationProperties：让 Spring 扫描并注册 LlmProperties，
 * 从 application.yml 中 kindergarten.llm 下读取配置并绑定到 Record 字段。
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
