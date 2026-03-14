package com.yu.ai.yuaicodemother.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yu.ai.yuaicodemother.ai.model.message.*;
import com.yu.ai.yuaicodemother.constant.AppConstant;
import com.yu.ai.yuaicodemother.core.builder.VueProjectBuilder;
import com.yu.ai.yuaicodemother.model.entity.User;
import com.yu.ai.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.yu.ai.yuaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.HashSet;

/**
 * JSON消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 处理TokenStream（VUE_PROJECT）
     * 解析JSON消息并重组成完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               Long appId, User loginUser) {
        //收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        //用于跟踪已经见过的工具ID，判断是否是第一次调用
        HashSet<Object> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    //解析每个json消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty)
                .doOnComplete(() -> {
                    //流式响应完成后，添加ai消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    //异步构造 Vue 项目
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_"+appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    //如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败：" + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 解析并收集TokenStream数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, HashSet<Object> seenToolIds) {
        //解析JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                //直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                //检查是否是第一次看到这个工具ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    //第一次调用这个工具，记录ID并完整返回工具信息
                    seenToolIds.add(toolId);
                    return "\n\n[选择工具] 写入文件\n\n";
                } else {
                    //不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String relativeFilePath = jsonObject.getStr("relativeFilePath");
                String suffix = FileUtil.getSuffix(relativeFilePath);
                String content = jsonObject.getStr("content");
                String result = String.format("""
                        [工具调用] 写入文件 %s
                        ```%s
                        %s
                        ```
                        """, relativeFilePath, suffix, content);
                //输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型：{}", typeEnum);
                return "";
            }
        }
    }
}
