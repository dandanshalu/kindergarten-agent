package com.kindergarten.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

/**
 * 大模型（LLM）调用服务，基于 LangChain4j。
 *
 * 使用 ChatModel 做同步调用、StreamingChatModel 做流式调用，
 * 支持任意 OpenAI 兼容 API（DeepSeek、通义、OpenAI 等）。
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private static final String SYSTEM_PROMPT = """
            你是面向幼儿园教师的智能助手，专门帮助老师完成教育教学、班级管理、家长沟通等文案创作。
            回答时请专业、实用，符合幼教场景。若用户未说明具体需求，可适当追问或给出示例建议。
            """;

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public LlmService(ChatModel openAiChatModel, StreamingChatModel openAiStreamingChatModel) {
        this.chatModel = openAiChatModel;
        this.streamingChatModel = openAiStreamingChatModel;
    }

    /**
     * 同步聊天：一次性获取完整回复。
     */
    public Mono<String> chat(String userMessage) {
        List<ChatMessage> messages = buildMessages(userMessage);
        log.info("LLM 同步请求, userMessage 长度: {}", userMessage != null ? userMessage.length() : 0);
        return Mono.fromCallable(() -> {
                    var response = chatModel.chat(messages);
                    String text = response.aiMessage() != null ? response.aiMessage().text() : null;
                    log.info("LLM 同步响应, 长度: {}", text != null ? text.length() : 0);
                    return text != null ? text : "";
                })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(90));
    }

    /**
     * 流式聊天：通过 LangChain4j StreamingChatModel 逐 token 返回，封装为 Flux。
     */
    public Flux<String> chatStream(String userMessage) {
        List<ChatMessage> messages = buildMessages(userMessage);
        log.info("LLM 流式请求, userMessage 长度: {}", userMessage != null ? userMessage.length() : 0);
        return Flux.<String>create(sink -> streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        if (partialResponse != null && !partialResponse.isEmpty()) {
                            sink.next(partialResponse);
                        }
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        log.debug("LLM 流式结束");
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.warn("LLM 流式错误: {}", error.getMessage());
                        sink.error(error);
                    }
                }))
                .timeout(Duration.ofSeconds(90));
    }

    private List<ChatMessage> buildMessages(String userMessage) {
        return List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userMessage)
        );
    }
}
