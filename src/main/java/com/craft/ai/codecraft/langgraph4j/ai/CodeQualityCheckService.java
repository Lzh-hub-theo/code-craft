package com.craft.ai.codecraft.langgraph4j.ai;

import com.craft.ai.codecraft.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface CodeQualityCheckService {

    /**
     * 代码质量检查
     * AI会分析代码并返回质量检查结果
     */
    @SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
    QualityResult checkQuality(@UserMessage String codeContent);

}
