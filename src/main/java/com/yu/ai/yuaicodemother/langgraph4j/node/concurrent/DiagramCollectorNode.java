package com.yu.ai.yuaicodemother.langgraph4j.node.concurrent;

import com.yu.ai.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yu.ai.yuaicodemother.langgraph4j.model.ImageResource;
import com.yu.ai.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yu.ai.yuaicodemother.langgraph4j.tools.MermaidDiagramTool;
import com.yu.ai.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class DiagramCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> diagrams = new ArrayList<>();

            try{
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                if(plan!=null&&plan.getDiagramTasks()!=null){
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    log.info("开始并发收集架构图片，任务数：{}", plan.getDiagramTasks().size());
                    for(ImageCollectionPlan.DiagramTask task:plan.getDiagramTasks()){
                        List<ImageResource> images = diagramTool.generateArchitectureDiagram(
                                task.mermaidCode(), task.description());
                        if(images!=null){
                            diagrams.addAll(images);
                        }
                    }
                    log.info("架构图生成完成，共收集到 {} 张图片",diagrams.size());
                }
            }catch(Exception e){
                log.error("生成架构图失败：{}",e.getMessage(),e);
            }

            context.setCurrentStep("架构图收集");
            context.setDiagrams(diagrams);
            return WorkflowContext.saveContext(context);
        });
    }
}
