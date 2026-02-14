package com.kindergarten.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM 配置属性，从 application.yml 的 kindergarten.llm 下读取。
 *
 * Java 16+ Record + @ConfigurationProperties：
 * Spring Boot 会把 yml 中的 kindergarten.llm.base-url 等自动绑定到 Record 字段。
 */
@ConfigurationProperties(prefix = "kindergarten.llm")
public record LlmProperties(
        String baseUrl,
        String apiKey,
        String model
) {
}
