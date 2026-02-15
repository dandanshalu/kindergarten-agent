package com.kindergarten.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 同步聊天响应 DTO（非流式时使用）。
 */
public record ChatResponse(
        String message,
        @JsonProperty("session_id") Long sessionId
) {
    public ChatResponse(String message) {
        this(message, null);
    }
}
