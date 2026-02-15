package com.kindergarten.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kindergarten.entity.Message;

import java.time.LocalDateTime;

/**
 * 消息 API 响应 DTO。
 */
public record MessageDto(
        Long id,
        String role,
        String content,
        @JsonProperty("created_at") String createdAt
) {
    public static MessageDto from(Message m) {
        return new MessageDto(
                m.getId(),
                m.getRole().name(),
                m.getContent(),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null
        );
    }
}
