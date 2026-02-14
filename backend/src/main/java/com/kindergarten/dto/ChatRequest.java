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
        @JsonProperty("doc_type_id") String docTypeId
) {
    /**
     * 紧凑构造器（Compact Constructor）：
     * 在自动生成的构造器逻辑之后执行，可用于校验或规范化。
     * 不需要写 this.message = message，Record 会自动完成字段赋值。
     */
    public ChatRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }

    /**
     * 若前端未传 docTypeId，提供默认值。
     * 使用静态工厂方法，便于扩展更多默认逻辑。
     */
    public static ChatRequest of(String message) {
        return new ChatRequest(message, "general");
    }
}
