package com.yu.ai.yuaicodemother.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamMessage {
    private String type;
}
