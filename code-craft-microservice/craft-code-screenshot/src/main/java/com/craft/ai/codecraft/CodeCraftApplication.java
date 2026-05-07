package com.craft.ai.codecraft;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class CodeCraftApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeCraftApplication.class, args);
    }

}
