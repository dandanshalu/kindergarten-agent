package com.kindergarten.config;

import com.kindergarten.service.LlmProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 相关配置：使用 LangChain4j 创建 OpenAI 兼容的 ChatModel / StreamingChatModel。
 *
 * baseUrl 需指向 /v1 的地址（如 https://api.deepseek.com/v1），
 * DeepSeek/通义等需设置 accumulateToolCallId(false)。
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    private static String ensureV1(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) return baseUrl;
        return baseUrl.endsWith("/v1") ? baseUrl : baseUrl.replaceAll("/?$", "") + "/v1";
    }

    /** 启动时校验 API Key 已通过环境变量注入，避免误部署无密钥实例。 */
    @Bean
    public ApplicationRunner llmApiKeyValidator(LlmProperties p) {
        return args -> {
            if (p.apiKey() == null || p.apiKey().isBlank()) {
                throw new IllegalStateException(
                    "LLM API Key 未配置。请设置环境变量 KINDERGARTEN_LLM_API_KEY，勿在配置文件中明文填写。参见 docs/LLM_CONFIG.md");
            }
        };
    }

    @Bean
    public ChatModel openAiChatModel(LlmProperties p) {
        return OpenAiChatModel.builder()
                .baseUrl(ensureV1(p.baseUrl()))
                .apiKey(p.apiKey())
                .modelName(p.model())
                .build();
    }

    @Bean
    public StreamingChatModel openAiStreamingChatModel(LlmProperties p) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(ensureV1(p.baseUrl()))
                .apiKey(p.apiKey())
                .modelName(p.model())
                .accumulateToolCallId(false)
                .build();
    }
}
