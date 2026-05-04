package com.craft.ai.codecraft.monitor;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorContextHolder {
    private static final TransmittableThreadLocal<MonitorContext> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

    public static void setContext(MonitorContext context){
        CONTEXT_HOLDER.set(context);
    }

    public static MonitorContext getContext(){
        return CONTEXT_HOLDER.get();
    }

    public static void clearContext(){
        CONTEXT_HOLDER.remove();
    }
}
