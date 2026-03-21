package com.yu.ai.yuaicodemother.langgraph4j.node.concurrent;

import com.yu.ai.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yu.ai.yuaicodemother.langgraph4j.model.ImageResource;
import com.yu.ai.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yu.ai.yuaicodemother.langgraph4j.tools.ImageSearchTool;
import com.yu.ai.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ContentImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> contentImages = new ArrayList<>();
            try{
                //获取任务，拿到图片搜索工具
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                if(plan!=null&& plan.getContentImageTasks()!=null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);

                    log.info("开始并发收集图片，任务数：{}", plan.getContentImageTasks().size());
                    for(ImageCollectionPlan.ImageSearchTask task:plan.getContentImageTasks()){
                        List<ImageResource> images = imageSearchTool.searchContentImages(task.query());
                        if(images!=null){
                            contentImages.addAll(images);
                        }
                    }
                    log.info("内容图片收集完成，共收集到 {} 张图片",contentImages.size());
                }
            }catch(Exception e){
                log.error("搜索内容图片失败：{}",e.getMessage(),e);
            }
            //将收集到的图片存储在上下文中
            context.setContentImages(contentImages);
            context.setCurrentStep("内容图片收集");
            return WorkflowContext.saveContext(context);
        });
    }
}
