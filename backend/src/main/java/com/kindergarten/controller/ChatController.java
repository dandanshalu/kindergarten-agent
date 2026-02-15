package com.kindergarten.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindergarten.dto.ChatRequest;
import com.kindergarten.dto.ChatResponse;
import com.kindergarten.entity.Message;
import com.kindergarten.service.LlmService;
import com.kindergarten.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 聊天 API 控制器。
 *
 * 流程：
 * 1. 解析/创建会话
 * 2. 保存用户消息
 * 3. 加载历史消息（含上下文）
 * 4. 调用 LLM 生成
 * 5. 保存助手消息
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final long SSE_TIMEOUT_MS = 90_000L;

    private final LlmService llmService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    public ChatController(LlmService llmService, SessionService sessionService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    private long currentUserId() {
        return SessionService.DEFAULT_USER_ID;
    }

    /**
     * 同步聊天
     */
    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ChatResponse>> chat(@RequestBody ChatRequest request) {
        return Mono.fromCallable(() -> {
            long sessionId = resolveSessionId(request.sessionId(), request.docTypeId());
            sessionService.saveUserMessage(sessionId, currentUserId(), request.message());
            var history = sessionService.getContextMessages(sessionId, currentUserId());
            updateSessionTitleIfFirstMessage(sessionId, request.message(), history.size());
            return new Object[] { sessionId, history };
        })
        .flatMap(tuple -> {
            long sessionId = (Long) ((Object[]) tuple)[0];
            @SuppressWarnings("unchecked")
            List<Message> history = (List<Message>) ((Object[]) tuple)[1];
            return llmService.chat(history)
                    .map(reply -> {
                        sessionService.saveAssistantMessage(sessionId, reply);
                        return ResponseEntity.ok(new ChatResponse(reply, sessionId));
                    });
        })
        .onErrorResume(e -> {
            var msg = "生成失败：" + (e.getMessage() != null ? e.getMessage() : "未知错误");
            return Mono.just(ResponseEntity.internalServerError().body(new ChatResponse(msg)));
        });
    }

    /**
     * 流式聊天：首条事件携带 sessionId，后续为 chunk。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        var emitter = new SseEmitter(SSE_TIMEOUT_MS);
        var fullReply = new AtomicReference<StringBuilder>(new StringBuilder());

        try {
            long sessionId = resolveSessionId(request.sessionId(), request.docTypeId());
            sessionService.saveUserMessage(sessionId, currentUserId(), request.message());
            var history = sessionService.getContextMessages(sessionId, currentUserId());
            updateSessionTitleIfFirstMessage(sessionId, request.message(), history.size());

            // 首条事件：会话 ID
            emitter.send(objectMapper.writeValueAsString(new StreamSessionEvent(sessionId)));

            llmService.chatStream(history).subscribe(
                    chunk -> {
                        try {
                            fullReply.get().append(chunk);
                            String payload = objectMapper.writeValueAsString(chunk);
                            emitter.send(payload);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    e -> emitter.completeWithError(e),
                    () -> {
                        try {
                            sessionService.saveAssistantMessage(sessionId, fullReply.get().toString());
                        } catch (Exception ignored) {}
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError((e) -> {});
        return emitter;
    }

    private long resolveSessionId(Long sessionId, String docTypeId) {
        if (sessionId != null && sessionId > 0) {
            var opt = sessionService.getSession(sessionId, currentUserId());
            if (opt.isPresent()) return sessionId;
        }
        var s = sessionService.createSession(currentUserId(), "新对话", docTypeId != null ? docTypeId : "general");
        return s.getId();
    }

    private void updateSessionTitleIfFirstMessage(long sessionId, String firstContent, int messageCount) {
        if (messageCount != 1) return;
        var title = sessionService.generateTitleFromFirstMessage(firstContent);
        sessionService.updateSession(sessionId, currentUserId(), title);
    }

    private record StreamSessionEvent(long sessionId) {}
}
