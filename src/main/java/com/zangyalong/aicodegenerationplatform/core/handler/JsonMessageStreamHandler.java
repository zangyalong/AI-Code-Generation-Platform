package com.zangyalong.aicodegenerationplatform.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zangyalong.aicodegenerationplatform.ai.model.message.*;
import com.zangyalong.aicodegenerationplatform.ai.tools.BaseTool;
import com.zangyalong.aicodegenerationplatform.ai.tools.ToolManager;
import com.zangyalong.aicodegenerationplatform.constant.AppConstant;
import com.zangyalong.aicodegenerationplatform.core.builder.VueProjectBuilder;
import com.zangyalong.aicodegenerationplatform.model.enums.ChatHistoryMessageTypeEnum;
import com.zangyalong.aicodegenerationplatform.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import com.zangyalong.aicodegenerationplatform.model.entity.User;
import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                .doOnComplete(() -> {
                    log.info("JsonMessageStreamHandler: 流式响应完成，appId: {}", appId);
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    
                    // 执行 Vue 项目构建（确保项目构建完成）
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    log.info("Vue项目生成完成，开始构建项目: {}", projectPath);
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    log.error("JsonMessageStreamHandler: 流式响应出错，appId: {}, 错误: {}", appId, error.getMessage(), error);
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        try {
            // 首先尝试解析为StreamMessage
            StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
            StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
            
            if (typeEnum != null) {
                // 处理已知的流式消息类型
                switch (typeEnum) {
                    case AI_RESPONSE -> {
                        AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                        String data = aiMessage.getData();
                        // 直接拼接响应
                        chatHistoryStringBuilder.append(data);
                        return data;
                    }
                    case TOOL_REQUEST -> {
                        ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                        String toolId = toolRequestMessage.getId();
                        String toolName = toolRequestMessage.getName();
                        // 检查是否是第一次看到这个工具 ID
                        if (toolId != null && !seenToolIds.contains(toolId)) {
                            // 第一次调用这个工具，记录 ID 并完整返回工具信息
                            seenToolIds.add(toolId);

                            // 根据工具名称获取工具实例
                            BaseTool tool = toolManager.getTool(toolName);

                            return tool.generateToolRequestResponse();
                        } else {
                            // 不是第一次调用这个工具，直接返回空
                            return "";
                        }
                    }
                    case TOOL_EXECUTED -> {
                        ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                        JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());

                        String toolName = toolExecutedMessage.getName();
                        // 根据工具名称获取工具实例并生成相应的结果格式
                        BaseTool tool = toolManager.getTool(toolName);
                        String result = tool.generateToolExecutedResult(jsonObject);

                        // 输出前端和要持久化的内容
                        String output = String.format("\n\n%s\n\n", result);
                        chatHistoryStringBuilder.append(output);
                        return output;
                    }
                    default -> {
                        log.error("不支持的消息类型: {}", typeEnum);
                        return "";
                    }
                }
            } else {
                // 如果不是已知的流式消息类型，尝试解析为原始ChatResponse消息
                JSONObject jsonObj = JSONUtil.parseObj(chunk);
                
                // 检查是否是AI assistant消息且包含content
                if ("assistant".equals(jsonObj.getStr("role")) && jsonObj.containsKey("content")) {
                    String content = jsonObj.getStr("content");
                    if (content != null && !content.trim().isEmpty()) {
                        chatHistoryStringBuilder.append(content);
                        return content;
                    }
                }
                
                log.debug("未处理的消息格式: {}", chunk);
                return "";
            }
        } catch (Exception e) {
            log.error("解析消息块失败: {}, 错误: {}", chunk, e.getMessage());
            return "";
        }
    }

}

