package com.craft.ai.codecraft.config.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import dev.langchain4j.http.client.sse.ServerSentEventListener;

import java.time.Duration;
import java.util.Map;

/**
 * HttpClientBuilder 装饰器：拦截发往 AI 的 chat/completions 请求体，
 * 注入 "thinking":{"type":"disabled"} 以关闭思考模式（MiniMax-M3 等模型通过此参数控制）。
 * langchain4j 1.1.0 的 ChatCompletionRequest 不支持 thinking 字段，只能在此层注入。
 */
public class ThinkingDisabledHttpClientBuilder implements HttpClientBuilder {

    private final HttpClientBuilder delegate;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ThinkingDisabledHttpClientBuilder(HttpClientBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Duration connectTimeout() {
        return delegate.connectTimeout();
    }

    @Override
    public HttpClientBuilder connectTimeout(Duration connectTimeout) {
        delegate.connectTimeout(connectTimeout);
        return this;
    }

    @Override
    public Duration readTimeout() {
        return delegate.readTimeout();
    }

    @Override
    public HttpClientBuilder readTimeout(Duration readTimeout) {
        delegate.readTimeout(readTimeout);
        return this;
    }

    @Override
    public HttpClient build() {
        return new ThinkingDisabledHttpClient(delegate.build());
    }

    /**
     * 在请求体 JSON 中注入 thinking.disabled 字段
     */
    private static HttpRequest withThinkingDisabled(HttpRequest request) {
        String body = request.body();
        if (body == null || body.isBlank()) {
            return request;
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            if (!root.isObject()) {
                return request;
            }
            ObjectNode obj = (ObjectNode) root;
            obj.putObject("thinking").put("type", "disabled");
            return HttpRequest.builder()
                    .method(request.method())
                    .url(request.url())
                    .headers(request.headers())
                    .body(MAPPER.writeValueAsString(obj))
                    .build();
        } catch (Exception e) {
            return request;
        }
    }

    private static class ThinkingDisabledHttpClient implements HttpClient {
        private final HttpClient delegate;

        ThinkingDisabledHttpClient(HttpClient delegate) {
            this.delegate = delegate;
        }

        @Override
        public SuccessfulHttpResponse execute(HttpRequest request) {
            return delegate.execute(withThinkingDisabled(request));
        }

        @Override
        public void execute(HttpRequest request,
                            ServerSentEventParser parser,
                            ServerSentEventListener listener) {
            delegate.execute(withThinkingDisabled(request), parser, listener);
        }
    }
}
