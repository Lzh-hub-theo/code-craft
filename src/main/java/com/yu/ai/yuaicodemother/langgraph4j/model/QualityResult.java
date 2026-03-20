package com.yu.ai.yuaicodemother.langgraph4j.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class QualityResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否通过质检
     */
    private Boolean isValid;

    /**
     * 错误信息
     */
    private List<String> errors;

    /**
     * 建议
     */
    private List<String> suggestions;

}
