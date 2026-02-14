package com.kindergarten.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：CORS 跨域、异步请求超时。
 *
 * 开发时前端（如 localhost:5173）和后端（localhost:8080）不同源，浏览器会拦截跨域请求。
 * 在此允许前端域名的跨域请求，便于本地联调。
 *
 * 聊天接口返回 Mono，由 Spring MVC 按异步请求处理；LLM 响应可能超过默认 30 秒，
 * 需将异步超时调大（与 LlmService 中 WebClient 的 90 秒一致），避免 AsyncRequestTimeoutException。
 */
@Configuration
public class WebConfig {

    private static final long ASYNC_TIMEOUT_MS = 90_000L;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }

            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setDefaultTimeout(ASYNC_TIMEOUT_MS);
            }
        };
    }
}
