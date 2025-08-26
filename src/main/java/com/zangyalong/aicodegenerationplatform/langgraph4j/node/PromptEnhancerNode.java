package com.zangyalong.aicodegenerationplatform.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageResource;
import com.zangyalong.aicodegenerationplatform.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");

            // 实际执行提示词增强逻辑
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = context.getImageListStr();
            List<ImageResource> imageList = context.getImageList();

            StringBuilder enhancePromptBuilder = new StringBuilder();
            enhancePromptBuilder.append(originalPrompt);

            if(CollUtil.isNotEmpty(imageList) || StrUtil.isNotBlank(imageListStr)) {
                enhancePromptBuilder.append("\n\n## 可用素材资源\n");
                enhancePromptBuilder.append("请在生成网站使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\n");
                if(CollUtil.isNotEmpty(imageList)) {
                    for(ImageResource image : imageList) {
                        enhancePromptBuilder.append("- ")
                                .append(image.getCategory().getText())
                                .append("：")
                                .append(image.getDescription())
                                .append("（")
                                .append(image.getUrl())
                                .append("）\n");
                    }
                } else {
                    enhancePromptBuilder.append(imageListStr);
                }
            }

            // 简单的假数据
            String enhancedPrompt = enhancePromptBuilder.toString();

            // 更新状态
            context.setCurrentStep("提示词增强");
            context.setEnhancedPrompt(enhancedPrompt);
            log.info("提示词增强完成，增强后长度: {} 字符", enhancedPrompt.length());
            return WorkflowContext.saveContext(context);
        });
    }
}

