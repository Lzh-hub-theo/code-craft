package com.craft.ai.codecraft.core.handler;

import com.yu.ai.codecraft.model.entity.User;
import com.yu.ai.codecraft.model.enums.CodeGenTypeEnum;
import com.yu.ai.codecraft.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 * 传统的Flux<String>流（HTML,MULTI_FILE）-> SimpleTextStreamHandler
 * TokenStream格式的复杂流（VUE_PROJECT）-> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {
    private static final SimpleTextStreamHandler simpleTextStreamHandler = new SimpleTextStreamHandler();

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  Long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch(codeGenType){
            case VUE_PROJECT ->
                jsonMessageStreamHandler.handle(originFlux,chatHistoryService,appId,loginUser);
            case HTML,MULTI_FILE ->
                simpleTextStreamHandler.handle(originFlux,chatHistoryService,appId,loginUser);
        };
    }
}
