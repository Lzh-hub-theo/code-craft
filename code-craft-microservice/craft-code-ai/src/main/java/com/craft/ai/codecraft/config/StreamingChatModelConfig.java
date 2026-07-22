package com.craft.ai.codecraft.config;

import com.craft.ai.codecraft.config.http.ThinkingDisabledHttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer maxTokens;
    private Double temperature;
    private Boolean logRequests;
    private Boolean logResponses;

//    @Resource
//    private AiModelMonitorListener aiModelMonitorListener;

    @Bean
    @Scope("prototype")
    public StreamingChatModel streamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
//                .listeners(List.of(aiModelMonitorListener))
                // 关闭思考模式：通过 HttpClient 装饰器在请求体注入 thinking.disabled
                .httpClientBuilder(new ThinkingDisabledHttpClientBuilder(new SpringRestClientBuilder()))
                // 全局禁用 gzip 压缩
                .customHeaders(Map.of("Accept-Encoding", "identity"))
                .build();
    }
}
