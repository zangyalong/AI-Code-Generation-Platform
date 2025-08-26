package com.zangyalong.aicodegenerationplatform.langgraph4j.node;

import com.zangyalong.aicodegenerationplatform.constant.AppConstant;
import com.zangyalong.aicodegenerationplatform.core.builder.VueProjectBuilder;
import com.zangyalong.aicodegenerationplatform.exception.BusinessException;
import com.zangyalong.aicodegenerationplatform.exception.ErrorCode;
import com.zangyalong.aicodegenerationplatform.langgraph4j.state.WorkflowContext;
import com.zangyalong.aicodegenerationplatform.model.enums.CodeGenTypeEnum;
import com.zangyalong.aicodegenerationplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            // 实际执行项目构建逻辑
            String generatedCodeDir = context.getGeneratedCodeDir();
            CodeGenTypeEnum generationType = context.getGenerationType();
            String buildResultDir;

            try {
                VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                boolean builSuccess = vueProjectBuilder.buildProject(generatedCodeDir);
                if(builSuccess){
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("Vue 项目构建成功，dist 目录: {}", buildResultDir);
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                }
            } catch (Exception e) {
                log.error("Vue 项目构建异常: {}", e.getMessage(), e);
                buildResultDir = generatedCodeDir; // 异常时返回原路径
            }

            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建完成，结果目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}

