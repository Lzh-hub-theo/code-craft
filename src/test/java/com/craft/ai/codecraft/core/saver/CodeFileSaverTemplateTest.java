package com.craft.ai.codecraft.core.saver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodeFileSaverTemplateTest {
    @Test
    void writeToFile() {
        MultiFileCodeFileSaverTemplate multiFileCodeFileSaverTemplate = new MultiFileCodeFileSaverTemplate();
        multiFileCodeFileSaverTemplate.writeToFile("D:\\code_output", "test.txt", "");
    }
}