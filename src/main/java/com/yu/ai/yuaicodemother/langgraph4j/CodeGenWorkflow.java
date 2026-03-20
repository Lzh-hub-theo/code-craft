package com.yu.ai.yuaicodemother.langgraph4j;

import com.yu.ai.yuaicodemother.exception.BusinessException;
import com.yu.ai.yuaicodemother.exception.ErrorCode;
import com.yu.ai.yuaicodemother.langgraph4j.model.QualityResult;
import com.yu.ai.yuaicodemother.langgraph4j.node.*;
import com.yu.ai.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yu.ai.yuaicodemother.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 简化版带状态定义的工作流 - 只定义状态结构，不实现具体流转
 */
@Slf4j
public class CodeGenWorkflow {

    public CompiledGraph<MessagesState<String>> createWorkflow(){
        try {
            return new MessagesStateGraph<String>()
                    // 添加节点 - 使用带状态感知的节点
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addNode("code_quality_check",CodeQualityCheckNode.create())

                    // 添加边
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")
                    // 新增质检条件边，根据质检结果决定下一步
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build","project_builder",// 需要构建的情况
                                    "skip_build",END,// 跳过构建直接结束
                                    "fail","code_generator"//质检失败，重新生成
                            ))
                    .addEdge("project_builder", END)

                    // 编译工作流
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    private String routeAfterQualityCheck(MessagesState<String> state){
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        //如果质检失败，重新生成代码
        if(qualityResult==null||!qualityResult.getIsValid()){
            log.error("代码质检失败，重新生成代码");
            return "fail";
        }
        //质检通过，使用原有的构建路由逻辑
        log.info("代码质检通过，继续后续流程");
        return routeBuildOrSkip(state);
    }

    private String routeBuildOrSkip(MessagesState<String> state){
        // 拿到自己维护的WorkflowContext上下文，拿到生成类型判断。
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenTypeEnum generationType = context.getGenerationType();
        if(generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE){
            return "skip_build";
        }
        return "build";
    }

    public WorkflowContext executeWorkflow(String originalPrompt){
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化 WorkflowContext - 只设置基本信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        // 显示工作流图
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行工作流");

        // 执行工作流
        int stepCounter = 1;
        WorkflowContext finalContext = null;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("工作流执行完成！");
        return finalContext;
    }

}
