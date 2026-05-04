package com.craft.ai.codecraft.langgraph4j.node.concurrent;

import com.craft.ai.codecraft.langgraph4j.model.ImageCollectionPlan;
import com.craft.ai.codecraft.langgraph4j.model.ImageResource;
import com.craft.ai.codecraft.langgraph4j.state.WorkflowContext;
import com.craft.ai.codecraft.langgraph4j.tools.UndrawIllustrationTool;
import com.craft.ai.codecraft.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class IllustrationCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> illustrations = new ArrayList<>();

            //防止报错，添加try-catch
            try{
                //定义一个收集到的图片列表
                //拿到计划，和工具，然后调用工具搜索图片
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                if(plan!=null&& plan.getIllustrationTasks()!=null){
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    log.info("开始并发收集插画图片，任务数：{}", plan.getIllustrationTasks().size());
                    for(ImageCollectionPlan.IllustrationTask task:plan.getIllustrationTasks()){
                        List<ImageResource> images = illustrationTool.searchIllustrations(task.query());
                        if(images!=null){
                            illustrations.addAll(images);
                        }
                    }
                    log.info("插画图片收集完成，共收集到 {} 张图片",illustrations.size());
                }

            }catch(Exception e){
                log.error("插画图片收集失败：{}",e.getMessage(),e);
            }

            //保存到上下文
            context.setCurrentStep("插画图片收集");
            context.setIllustrations(illustrations);
            return WorkflowContext.saveContext(context);
        });
    }
}
