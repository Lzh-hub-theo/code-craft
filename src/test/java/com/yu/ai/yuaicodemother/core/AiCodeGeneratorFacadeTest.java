package com.yu.ai.yuaicodemother.core;

import com.yu.ai.yuaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode(){
        File file = aiCodeGeneratorFacade.generateAndSaveCode("做个程序员鱼皮的博客，不超过20行", CodeGenTypeEnum.HTML, 1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("做个程序员鱼皮的博客，不超过20行", CodeGenTypeEnum.HTML, 1L);
        //阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        //验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("做个程序员鱼皮的博客，不超过20行", CodeGenTypeEnum.VUE_PROJECT, 1L);
        //阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        //验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);

    }

}