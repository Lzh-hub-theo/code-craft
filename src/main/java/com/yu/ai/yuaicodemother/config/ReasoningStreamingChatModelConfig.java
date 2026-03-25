package com.yu.ai.yuaicodemother.config;

import com.yu.ai.yuaicodemother.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer maxTokens;
    private Double temperature;
    private Boolean logRequests = false;
    private Boolean logResponses = false;

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
//         为了测试方便临时修改
//        final String modelName = "deepseek-chat";
//        final int maxTokens = 8192;
//         生产环境使用
//        final String modelName = "deepseek-reasoner";
//        final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(true)
                .logResponses(true)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }
}
