package com.kindergarten.service;

import com.kindergarten.entity.Message;
import com.kindergarten.entity.Message.Role;
import com.kindergarten.entity.Session;
import com.kindergarten.repository.MessageRepository;
import com.kindergarten.repository.SessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 会话服务：对话会话、消息历史、上下文管理。
 * 认证未实现前，使用 DEFAULT_USER_ID 作为占位。
 */
@Service
public class SessionService {

    /** 认证未实现前的默认用户 ID */
    public static final long DEFAULT_USER_ID = 1L;

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;

    public SessionService(SessionRepository sessionRepository, MessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 会话列表（分页）
     */
    public Page<Session> listSessions(long userId, int page, int size) {
        if (userId <= 0) userId = DEFAULT_USER_ID;
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(
                userId,
                PageRequest.of(page, size, Sort.unsorted())
        );
    }

    /**
     * 创建会话
     */
    @Transactional
    public Session createSession(Long userId, String title, String docTypeId) {
        if (userId == null || userId <= 0) userId = DEFAULT_USER_ID;
        var session = new Session(userId, title != null ? title : "新对话", docTypeId);
        return sessionRepository.save(session);
    }

    /**
     * 获取会话详情（含消息）
     */
    public Optional<Session> getSession(Long id, Long userId) {
        if (userId == null || userId <= 0) userId = DEFAULT_USER_ID;
        var opt = sessionRepository.findById(id);
        if (opt.isEmpty()) return Optional.empty();
        var s = opt.get();
        if (!s.getUserId().equals(userId)) return Optional.empty();
        return Optional.of(s);
    }

    /**
     * 获取会话的消息历史
     */
    public List<Message> getMessages(Long sessionId, Long userId) {
        if (!getSession(sessionId, userId).isPresent()) {
            return List.of();
        }
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * 更新会话（标题等）
     */
    @Transactional
    public Optional<Session> updateSession(Long id, Long userId, String title) {
        var opt = getSession(id, userId);
        if (opt.isEmpty()) return Optional.empty();
        var s = opt.get();
        if (title != null && !title.isBlank()) s.setTitle(title);
        return Optional.of(sessionRepository.save(s));
    }

    /**
     * 删除会话
     */
    @Transactional
    public boolean deleteSession(Long id, Long userId) {
        if (userId == null || userId <= 0) userId = DEFAULT_USER_ID;
        if (!sessionRepository.existsByIdAndUserId(id, userId)) return false;
        messageRepository.deleteBySessionId(id);
        sessionRepository.deleteById(id);
        return true;
    }

    /**
     * 保存用户消息
     */
    @Transactional
    public Message saveUserMessage(Long sessionId, Long userId, String content) {
        if (!getSession(sessionId, userId).isPresent()) {
            throw new IllegalArgumentException("会话不存在或无权限");
        }
        var msg = new Message(sessionId, Role.user, content);
        return messageRepository.save(msg);
    }

    /**
     * 保存助手消息
     */
    @Transactional
    public Message saveAssistantMessage(Long sessionId, String content) {
        var msg = new Message(sessionId, Role.assistant, content);
        return messageRepository.save(msg);
    }

    /**
     * 获取会话的历史消息（供 LLM 上下文使用）
     */
    public List<Message> getContextMessages(Long sessionId, Long userId) {
        return getMessages(sessionId, userId);
    }

    /**
     * 根据首条用户消息生成简短标题（截取前 20 字）
     */
    public String generateTitleFromFirstMessage(String firstUserContent) {
        if (firstUserContent == null || firstUserContent.isBlank()) return "新对话";
        var t = firstUserContent.trim().replaceAll("\\s+", " ");
        return t.length() > 20 ? t.substring(0, 20) + "…" : t;
    }
}
