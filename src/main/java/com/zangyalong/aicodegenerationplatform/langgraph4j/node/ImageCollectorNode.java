package com.zangyalong.aicodegenerationplatform.langgraph4j.node;

import cn.hutool.ai.core.Message;
import com.zangyalong.aicodegenerationplatform.langgraph4j.ai.ImageCollectionService;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageResource;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.enums.ImageCategoryEnum;
import com.zangyalong.aicodegenerationplatform.langgraph4j.state.WorkflowContext;
import com.zangyalong.aicodegenerationplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state ->{
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：图片收集");

            // 实际执行的图片收集逻辑
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            log.info("图片收集完成");
            return WorkflowContext.saveContext(context);
        });
    }
}
