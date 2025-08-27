package com.zangyalong.aicodegenerationplatform.langgraph4j.ai;

import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.FreepikIllustrationTool;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.ImageSearchTool;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.LogoGeneratorTool;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.MermaidDiagramTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private FreepikIllustrationTool freepikIllustrationTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集 AI 服务
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .chatModel(chatModel)
                .tools(
                        imageSearchTool,
                        freepikIllustrationTool,
                        mermaidDiagramTool,
                        logoGeneratorTool
                )
                .build();
    }
}

