package com.zangyalong.aicodegenerationplatform.langgraph4j.state;

import com.zangyalong.aicodegenerationplatform.model.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageResource;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowContext implements Serializable {

    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    private String currentStep;

    private String originalPrompt;

    private String imageListStr;

    /**
     * 图片资源列表
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum generationType;

    /**
     * 生成的代码目录
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     */
    private String buildResultDir;

    /**
     * 错误信息
     */
    private String errorMessage;

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 上下文操作方法 ==========

    /**
     * 从 MessagesState 中获取 WorkflowContext
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }
    /**
     * 将 WorkflowContext 保存到 MessagesState 中
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}
