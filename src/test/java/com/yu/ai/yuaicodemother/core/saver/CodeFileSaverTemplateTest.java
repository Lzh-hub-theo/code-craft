package com.yu.ai.yuaicodemother.core.saver;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodeFileSaverTemplateTest {
    @Test
    void writeToFile() {
        MultiFileCodeFileSaverTemplate multiFileCodeFileSaverTemplate = new MultiFileCodeFileSaverTemplate();
        multiFileCodeFileSaverTemplate.writeToFile("D:\\code_output", "test.txt", "");
    }
}