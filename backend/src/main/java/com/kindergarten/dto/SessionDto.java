package com.kindergarten.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kindergarten.entity.Session;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话 API 响应 DTO。
 */
public record SessionDto(
        Long id,
        @JsonProperty("user_id") Long userId,
        String title,
        @JsonProperty("doc_type_id") String docTypeId,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        List<MessageDto> messages
) {
    public static SessionDto from(Session s, List<MessageDto> messages) {
        return new SessionDto(
                s.getId(),
                s.getUserId(),
                s.getTitle(),
                s.getDocTypeId(),
                s.getCreatedAt() != null ? s.getCreatedAt().toString() : null,
                s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null,
                messages
        );
    }

    public static SessionDto from(Session s) {
        return from(s, null);
    }
}
