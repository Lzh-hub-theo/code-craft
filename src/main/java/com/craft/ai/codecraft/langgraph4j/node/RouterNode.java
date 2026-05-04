package com.craft.ai.codecraft.langgraph4j.node;

import cn.hutool.extra.spring.SpringUtil;
import com.craft.ai.codecraft.ai.AiCodeGenTypeRoutingService;
import com.craft.ai.codecraft.ai.AiCodeGenTypeRoutingServiceFactory;
import com.craft.ai.codecraft.langgraph4j.state.WorkflowContext;
import com.craft.ai.codecraft.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            CodeGenTypeEnum generationType;
            try{
                AiCodeGenTypeRoutingServiceFactory factory = SpringUtil.getBean(AiCodeGenTypeRoutingServiceFactory.class);
                AiCodeGenTypeRoutingService routingService = factory.createAiCodeGenTypeRoutingService();
                generationType = routingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("AI 智能路由完成，选择类型：{}（{}）",generationType.getValue(),generationType.getText());
            }catch (Exception e){
                log.error("AI 智能路由失败，使用默认的HTML：{}", e.getMessage());
                generationType= CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}
