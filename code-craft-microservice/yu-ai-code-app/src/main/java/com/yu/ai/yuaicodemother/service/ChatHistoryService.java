package com.yu.ai.yuaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yu.ai.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yu.ai.yuaicodemother.model.entity.ChatHistory;
import com.yu.ai.yuaicodemother.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 *  服务层。
 *
 * @author 鱼皮
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 添加对话消息
     * @param appId 应用ID
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户ID
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
}
