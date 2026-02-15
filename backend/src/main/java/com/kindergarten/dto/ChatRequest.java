package com.kindergarten.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 聊天请求体 DTO。
 *
 * Java 16+ Record 语法：
 * record 是「不可变数据载体」的简写，自动生成：
 * - 全参构造器、getter（如 message()）、equals、hashCode、toString
 * 等同于以前的：
 * public final class ChatRequest {
 *     private final String message;
 *     public ChatRequest(String message) { this.message = message; }
 *     public String getMessage() { return message; }
 *     // ... equals, hashCode, toString
 * }
 */
public record ChatRequest(
        String message,
        @JsonProperty("session_id") Long sessionId,
        @JsonProperty("doc_type_id") String docTypeId
) {
    /**
     * 紧凑构造器（Compact Constructor）：校验 message 非空。
     */
    public ChatRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }

    public static ChatRequest of(String message) {
        return new ChatRequest(message, null, "general");
    }
}
