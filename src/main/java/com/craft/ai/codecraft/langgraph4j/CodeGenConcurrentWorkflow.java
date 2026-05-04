package com.craft.ai.codecraft.langgraph4j;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.json.JSONUtil;
import com.craft.ai.codecraft.exception.BusinessException;
import com.craft.ai.codecraft.exception.ErrorCode;
import com.craft.ai.codecraft.langgraph4j.model.QualityResult;
import com.craft.ai.codecraft.langgraph4j.node.*;
import com.craft.ai.codecraft.langgraph4j.node.concurrent.DiagramCollectorNode;
import com.craft.ai.codecraft.langgraph4j.node.concurrent.IllustrationCollectorNode;
import com.craft.ai.codecraft.langgraph4j.node.concurrent.ImageAggregatorNode;
import com.craft.ai.codecraft.langgraph4j.node.concurrent.LogoCollectorNode;
import com.craft.ai.codecraft.langgraph4j.state.WorkflowContext;
import com.craft.ai.codecraft.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Slf4j
public class CodeGenConcurrentWorkflow {

    public CompiledGraph<MessagesState<String>> createWorkflow(){
        try {
            return new MessagesStateGraph<String>()
                    // 添加节点 - 使用带状态感知的节点
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())

                    //添加并发节点
                    .addNode("content_image_collector", ImageCollectorNode.create())
                    .addNode("illustration_collector", IllustrationCollectorNode.create())
                    .addNode("diagram_collector", DiagramCollectorNode.create())
                    .addNode("logo_collector", LogoCollectorNode.create())
                    .addNode("image_aggregator", ImageAggregatorNode.create())

                    // 添加边
                    .addEdge(START, "image_plan")

                    //并发分支
                    .addEdge("image_plan","content_image_collector")
                    .addEdge("image_plan","illustration_collector")
                    .addEdge("image_plan","diagram_collector")
                    .addEdge("image_plan","logo_collector")

                    //汇聚：所有节点汇聚到聚合器
                    .addEdge("content_image_collector","image_aggregator")
                    .addEdge("illustration_collector","image_aggregator")
                    .addEdge("diagram_collector","image_aggregator")
                    .addEdge("logo_collector","image_aggregator")

                    .addEdge("image_aggregator", "prompt_enhancer")
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

        //配置并发执行
        ExecutorService pool = ExecutorBuilder.create()
                .setCorePoolSize(10)
                .setMaxPoolSize(20)
                .setWorkQueue(new LinkedBlockingQueue<>(100))
                .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("Parallel-Image-Collect").build())
                .build();
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .addParallelNodeExecutor("image_plan",pool)
                .build();

        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext),
                runnableConfig)) {
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

    /**
     * 执行工作流（Flux 流式输出版本）
     */
    public Flux<String> executeWorkflowWithFlux(String originalPrompt){
        return Flux.create(sink -> {
            Thread.startVirtualThread(() -> {
                try{
                    CompiledGraph<MessagesState<String>> workflow = createWorkflow();
                    WorkflowContext initialContext = WorkflowContext.builder()
                            .originalPrompt(originalPrompt)
                            .currentStep("初始化")
                            .build();
                    sink.next(formatSseEvent("workflow_start",Map.of(
                            "message","开始执行代码生成工作流",
                            "originalPrompt",originalPrompt
                    )));
                    GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                    log.info("工作流图：\n{}",graph.content());

                    int stepCounter = 1;
                    for(NodeOutput<MessagesState<String>> step:workflow.stream(
                            Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY,initialContext))){
                        log.info("--- 第 {} 步完成 ---",stepCounter);
                        WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                        if(currentContext!=null){
                            sink.next(formatSseEvent("step_completed",Map.of(
                                    "stepNumber",stepCounter,
                                    "currentStep",currentContext.getCurrentStep()
                            )));
                            log.info("当前步骤上下文：{}",currentContext);
                        }
                        stepCounter++;
                    }
                    sink.next(formatSseEvent("workflow_completed",Map.of(
                            "message","代码生成工作流执行完成！"
                    )));
                    log.info("代码生成工作流执行完成！");
                    sink.complete();
                }catch(Exception e){
                    log.error("工作流执行失败：{}",e.getMessage(),e);
                    sink.next(formatSseEvent("workflow_error",Map.of(
                            "error",e.getMessage(),
                            "message","工作流执行失败"
                    )));
                    sink.error(e);
                }
            });
        });
    }

    /**
     * 格式化SSE事件的辅助方法
     */
    private String formatSseEvent(String eventType, Object data){
        try{
            String jsonData = JSONUtil.toJsonStr(data);
            return "event: "+eventType+"\ndata: "+jsonData+"\n\n";
        }catch(Exception e){
            log.error("格式化SSE事件失败：{}",e.getMessage(),e);
            return "event: error\ndata: {\"error\":\"格式化失败\"}\n\n";
        }
    }
}
