package com.kindergarten.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 对话会话实体。
 * 认证未实现前，userId 使用占位值（如 1L）。
 */
@Entity
@Table(name = "chat_session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "doc_type_id", length = 64)
    private String docTypeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        var now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Session() {}

    public Session(Long userId, String title, String docTypeId) {
        this.userId = userId;
        this.title = title != null ? title : "新对话";
        this.docTypeId = docTypeId != null ? docTypeId : "general";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocTypeId() { return docTypeId; }
    public void setDocTypeId(String docTypeId) { this.docTypeId = docTypeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
