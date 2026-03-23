package com.yu.ai.yuaicodemother.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 服务部署服务
 * 需要Node.js环境
 *
 * @author 鱼皮
 */
@Service
public class ServeDeployService {

    private static final String CODE_BASE_DIR="/tmp/deploy";
    private static final int SERVER_PORT=3000;
    private static Process serveProcess;

    /**
     * 启动Serve服务
     */
    public void startServeService(){
        try{
            if(serveProcess==null|| !serveProcess.isAlive()){
                ProcessBuilder pb = new ProcessBuilder(
                        "npx", "serve", CODE_BASE_DIR, "-p", String.valueOf(SERVER_PORT)
                );
                pb.redirectErrorStream(true);
                serveProcess = pb.start();
                System.out.println("Serve service start on port "+SERVER_PORT);
            }
        } catch (Exception e){
            throw new RuntimeException("Failed to start serve service",e);
        }
    }

    /**
     * 停止Serve服务
     */
    public void stopServeService(){
        if(serveProcess!=null && serveProcess.isAlive()){
            serveProcess.destroy();
            try {
                serveProcess.waitFor(5, TimeUnit.SECONDS);
                System.out.println("Serve service stopped");
            } catch (InterruptedException e) {
                serveProcess.destroyForcibly();
                System.out.println("Serve service force stopped");
            }
        }
    }
}
