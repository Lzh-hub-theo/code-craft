package com.craft.ai.codecraft.langgraph4j.ai;

import com.craft.ai.codecraft.langgraph4j.tools.ImageSearchTool;
import com.craft.ai.codecraft.langgraph4j.tools.LogoGeneratorTool;
import com.craft.ai.codecraft.langgraph4j.tools.MermaidDiagramTool;
import com.craft.ai.codecraft.langgraph4j.tools.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集ai服务
     */
    @Bean
    public ImageCollectionService imageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .chatModel(chatModel)
                .tools(
                        imageSearchTool,
                        mermaidDiagramTool,
                        undrawIllustrationTool,
                        logoGeneratorTool
                )
                .build();
    }
}
