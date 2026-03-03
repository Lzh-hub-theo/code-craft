package com.yu.ai.yuaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AppAdminUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
