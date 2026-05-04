package com.yu.ai.codecraft.model.dto.chathistory;

import com.yu.ai.codecraft.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 消息
     */
    private String message;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于这些时间的记录
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 1L;
}
