package com.kindergarten.controller;

import com.kindergarten.dto.ChatRequest;
import com.kindergarten.dto.ChatResponse;
import com.kindergarten.service.LlmService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 聊天 API 控制器。
 *
 * Agent 流程简述：
 * 1. 用户在前端输入消息 -> 2. 前端 POST /api/chat
 * 3. 本控制器接收 -> 4. LlmService 转发给 LLM
 * 5. LLM 生成回复 -> 6. 返回给前端展示
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final LlmService llmService;

    public ChatController(LlmService llmService) {
        this.llmService = llmService;
    }

    /**
     * 同步聊天：用户发送消息，等待完整回复后返回。
     *
     * Java 21 / Spring WebFlux：
     * - ResponseEntity<Mono<T>>：Spring 能正确处理 Mono，在订阅时执行请求
     * - 也可直接返回 Mono<ChatResponse>，Spring 会自动处理
     */
    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ChatResponse>> chat(@RequestBody ChatRequest request) {
        return llmService.chat(request.message())
                .map(ChatResponse::new)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    // 错误时返回友好提示
                    var msg = "生成失败：" + (e.getMessage() != null ? e.getMessage() : "未知错误");
                    return Mono.just(ResponseEntity.internalServerError()
                            .body(new ChatResponse(msg)));
                });
    }
}
