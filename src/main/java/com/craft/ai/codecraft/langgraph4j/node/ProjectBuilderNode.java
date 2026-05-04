package com.craft.ai.codecraft.langgraph4j.node;

import com.craft.ai.codecraft.core.builder.VueProjectBuilder;
import com.craft.ai.codecraft.exception.BusinessException;
import com.craft.ai.codecraft.exception.ErrorCode;
import com.craft.ai.codecraft.langgraph4j.state.WorkflowContext;
import com.craft.ai.codecraft.model.enums.CodeGenTypeEnum;
import com.craft.ai.codecraft.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            String generatedCodeDir = context.getGeneratedCodeDir();
            CodeGenTypeEnum generationType = context.getGenerationType();
            String buildResultDir;
            //一定是vue项目类型
            try {
                VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                boolean buildSuccess = vueProjectBuilder.buildProject(generatedCodeDir);
                if (buildSuccess) {
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("项目构建成功，dist目录: {}", buildResultDir);
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目构建失败");
                }
            } catch (Exception e) {
                log.error("项目构建失败: {}", e.getMessage(), e);
                buildResultDir = generatedCodeDir;
            }

            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建完成，结果目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
