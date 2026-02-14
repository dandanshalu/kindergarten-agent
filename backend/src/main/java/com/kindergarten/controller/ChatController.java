package com.kindergarten.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindergarten.dto.ChatRequest;
import com.kindergarten.dto.ChatResponse;
import com.kindergarten.service.LlmService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * 聊天 API 控制器。
 *
 * Agent 流程简述：
 * 1. 用户在前端输入消息 -> 2. 前端 POST /api/chat 或 /api/chat/stream
 * 3. 本控制器接收 -> 4. LlmService 转发给 LLM
 * 5. LLM 生成回复 -> 6. 同步接口一次性返回，流式接口通过 SSE 逐段推送
 *
 * 流式说明：SSE 规范中多行 data 会按行拆成多条，导致前端收到的 chunk 丢失换行。
 * 因此将每个 chunk 按 JSON 字符串序列化后发送，前端解析 JSON 后即可还原换行。
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final long SSE_TIMEOUT_MS = 90_000L;

    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public ChatController(LlmService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    /**
     * 同步聊天：用户发送消息，等待完整回复后返回。
     */
    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ChatResponse>> chat(@RequestBody ChatRequest request) {
        return llmService.chat(request.message())
                .map(ChatResponse::new)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    var msg = "生成失败：" + (e.getMessage() != null ? e.getMessage() : "未知错误");
                    return Mono.just(ResponseEntity.internalServerError()
                            .body(new ChatResponse(msg)));
                });
    }

    /**
     * 流式聊天：chunk 以 JSON 字符串发送以保留换行（与 DeepSeek/OpenAI 流式 delta 语义一致）。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        var emitter = new SseEmitter(SSE_TIMEOUT_MS);
        llmService.chatStream(request.message())
                .subscribe(
                        chunk -> {
                            try {
                                String payload = objectMapper.writeValueAsString(chunk);
                                emitter.send(payload);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("序列化 chunk 失败", e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        e -> emitter.completeWithError(e),
                        emitter::complete
                );
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError((e) -> {});
        return emitter;
    }
}
