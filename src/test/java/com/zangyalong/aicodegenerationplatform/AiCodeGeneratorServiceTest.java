package com.zangyalong.aicodegenerationplatform;

import com.zangyalong.aicodegenerationplatform.ai.AiCodeGeneratorService;
import com.zangyalong.aicodegenerationplatform.ai.model.HtmlCodeResult;
import com.zangyalong.aicodegenerationplatform.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员mz的工作记录小工具,代码不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个程序员mz的留言板,代码不超过50行");
        Assertions.assertNotNull(multiFileCode);
    }
}

