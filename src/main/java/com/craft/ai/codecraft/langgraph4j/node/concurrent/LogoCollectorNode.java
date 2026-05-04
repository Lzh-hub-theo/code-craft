package com.craft.ai.codecraft.langgraph4j.node.concurrent;

import com.craft.ai.codecraft.langgraph4j.model.ImageCollectionPlan;
import com.craft.ai.codecraft.langgraph4j.model.ImageResource;
import com.craft.ai.codecraft.langgraph4j.state.WorkflowContext;
import com.craft.ai.codecraft.langgraph4j.tools.LogoGeneratorTool;
import com.craft.ai.codecraft.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class LogoCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> logos=new ArrayList<>();

            try{
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                if(plan!=null&&plan.getLogoTasks()!=null){
                    LogoGeneratorTool logoGeneratorTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    log.info("开始并发生成Logo，任务数：{}", plan.getLogoTasks().size());
                    for(ImageCollectionPlan.LogoTask task:plan.getLogoTasks()){
                        List<ImageResource> images = logoGeneratorTool.generateLogos(task.description());
                        if(images!=null){
                            logos.addAll(images);
                        }
                    }
                    log.info("架构图生成完成，共收集到 {} 张图片",logos.size());
                }
            }catch(Exception e){
                log.error("生成Logo失败：{}",e.getMessage(),e);
            }

            context.setCurrentStep("Logo生成");
            context.setLogos(logos);
            return WorkflowContext.saveContext(context);
        });
    }
}
