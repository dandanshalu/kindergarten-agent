package com.kindergarten.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Nacos 注册延迟监听器。
 * 给 gRPC 客户端预留连接时间，避免 "Client not connected, current status:STARTING"。
 */
@Component
@ConditionalOnProperty(prefix = "kindergarten.nacos", name = "registration-delay-ms")
public class NacosRegistrationDelayListener implements ApplicationListener<WebServerInitializedEvent>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(NacosRegistrationDelayListener.class);

    private final int delayMs;

    public NacosRegistrationDelayListener(
            @org.springframework.beans.factory.annotation.Value("${kindergarten.nacos.registration-delay-ms:5000}") int delayMs) {
        this.delayMs = delayMs > 0 ? delayMs : 5000;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        log.info("等待 {} ms 后允许 Nacos 注册（便于 gRPC 客户端完成连接）", delayMs);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Nacos 注册延迟被中断");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
