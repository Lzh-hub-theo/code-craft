package com.yu.ai.yuaicodemother.lifecycle;

import com.yu.ai.yuaicodemother.service.ServeDeployService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 方案一：通过启动服务时开启Node.js的serve服务来实现网页部署功能
 */
//@Component
public class ServeLifecycleManager {

//    @Autowired
    private ServeDeployService serveDeployService;

    /**
     * SpringBoot启动完成后启动serve服务
     */
//    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(){
        serveDeployService.startServeService();
    }

    /**
     * springboot 关闭时停止serve服务
     */
//    @PreDestroy
    public void onApplicationShutdown(){
        System.out.println("shutting down serve service...");
        serveDeployService.stopServeService();
    }
}
