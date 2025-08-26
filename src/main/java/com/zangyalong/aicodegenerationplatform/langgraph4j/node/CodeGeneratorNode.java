package com.zangyalong.aicodegenerationplatform.langgraph4j.node;

import com.zangyalong.aicodegenerationplatform.constant.AppConstant;
import com.zangyalong.aicodegenerationplatform.core.AiCodeGeneratorFacade;
import com.zangyalong.aicodegenerationplatform.langgraph4j.state.WorkflowContext;
import com.zangyalong.aicodegenerationplatform.model.enums.CodeGenTypeEnum;
import com.zangyalong.aicodegenerationplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");

            // 实际执行代码生成逻辑
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum generationType = context.getGenerationType();
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {} ({})", generationType.getValue(), generationType.getText());

            // TODO:后续还需要整合到业务中
            Long appId = 0L;
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            codeStream.blockLast(Duration.ofMinutes(10));

            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,
                    generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}

