package com.kindergarten.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 大模型（LLM）调用服务。
 *
 * Agent 核心概念：
 * - LLM（Large Language Model）：大语言模型，如 GPT、通义千问等
 * - Prompt：发给模型的「指令 + 上下文 + 用户输入」，模型据此生成回复
 * - Token：模型处理文本的基本单位，中文约 1–2 字/token
 * - 流式（Streaming）：逐 token 返回，用户能边看边等，体验更好
 */
@Service
public class LlmService {

    private final WebClient webClient;
    private final LlmProperties properties;

    public LlmService(WebClient.Builder builder, LlmProperties properties) {
        this.webClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.properties = properties;
    }

    /**
     * 同步聊天：一次性获取完整回复。
     * 适合简单场景，用户等待几秒后看到完整内容。
     *
     * Mono：Reactor 中的「0 或 1 个元素」的流，类似 Optional 的异步版。
     */
    public Mono<String> chat(String userMessage) {
        var messages = buildMessages(userMessage);
        var body = Map.of(
                "model", properties.model(),
                "messages", messages,
                "stream", false
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> {
                    var content = node.path("choices").get(0).path("message").path("content").asText();
                    return content != null ? content : "";
                })
                .timeout(Duration.ofSeconds(90));
    }

    /**
     * 流式聊天：向 LLM 发送消息，以 Flux（流）形式逐段返回内容。
     *
     * Java 21 / Reactor 概念：
     * - Flux<String>：类似 Stream，但支持异步和背压，是响应式编程的核心类型
     * - 这里每个 onNext 携带一小段文本（通常是一个 token 或几个 token）
     */
    public Flux<String> chatStream(String userMessage) {
        var messages = buildMessages(userMessage);
        var body = Map.of(
                "model", properties.model(),
                "messages", messages,
                "stream", true
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                /*
                 * SSE 格式说明：OpenAI 流式返回每行形如 "data: {...}\n"
                 * 需要按行解析，提取 content 中的 delta 文本
                 */
                .flatMap(this::parseSseLine)
                .timeout(Duration.ofSeconds(90));
    }

    /**
     * 解析单行 SSE 数据。
     * OpenAI 流式返回格式：每行形如 "data: {\"choices\":[{\"delta\":{\"content\":\"你\"}}]}\n"
     * 需要提取 delta.content 作为本次 token。
     */
    private Flux<String> parseSseLine(String line) {
        if (line == null || !line.startsWith("data: ")) {
            return Flux.empty();
        }
        var json = line.substring(6);
        if ("[DONE]".equals(json.trim())) {
            return Flux.empty();
        }
        // 简单解析：提取 "content" 中的 "delta" 文本
        try {
            var content = extractDeltaContent(json);
            return content != null && !content.isEmpty()
                    ? Flux.just(content)
                    : Flux.empty();
        } catch (Exception e) {
            return Flux.empty();
        }
    }

    /**
     * 从 OpenAI SSE JSON 中提取 delta.content。
     * 生产环境建议用 Jackson 等 JSON 库做完整解析。
     */
    private String extractDeltaContent(String json) {
        // 简化实现：正则提取 "content":"xxx"
        var pattern = java.util.regex.Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*)\"");
        var matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        }
        return null;
    }

    /**
     * 构建 messages 列表。
     * 使用 Java 15+ 文本块（Text Blocks）：
     * """ ... """ 支持多行字符串，自动处理换行和缩进，比 "a" + "b" 更清晰。
     */
    private List<Map<String, Object>> buildMessages(String userMessage) {
        return List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                你是面向幼儿园教师的智能助手，专门帮助老师完成教育教学、班级管理、家长沟通等文案创作。
                                回答时请专业、实用，符合幼教场景。若用户未说明具体需求，可适当追问或给出示例建议。
                                """
                ),
                Map.of("role", "user", "content", userMessage)
        );
    }
}
