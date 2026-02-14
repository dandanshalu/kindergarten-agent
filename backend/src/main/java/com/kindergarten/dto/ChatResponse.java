package com.kindergarten.dto;

/**
 * 同步聊天响应 DTO（非流式时使用）。
 *
 * Record 的另一个好处：用于 API 请求/响应时，JSON 序列化（Jackson）
 * 会自动把 message() 映射为 JSON 字段 "message"。
 */
public record ChatResponse(String message) {
}
