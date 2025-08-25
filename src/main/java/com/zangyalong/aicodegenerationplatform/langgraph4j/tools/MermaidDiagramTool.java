package com.zangyalong.aicodegenerationplatform.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.zangyalong.aicodegenerationplatform.exception.BusinessException;
import com.zangyalong.aicodegenerationplatform.exception.ErrorCode;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageResource;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.enums.ImageCategoryEnum;
import com.zangyalong.aicodegenerationplatform.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;

    @Tool("将Mermaid代码转成架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码")String mermaidCode,
                                                      @P("架构图描述")String description) {
        if(StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            File diagramFile = convertMermaidToSvg(mermaidCode);
            String keyName = String.format("/mermaid/%s/%s", RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);

            FileUtil.del(diagramFile);
            if(StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private File convertMermaidToSvg(String mermaidCode) {
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, tempInputFile);

        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
        String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
        String cmdLine = String.format("%s -i %s -o %s -b transparent",
                command,
                tempInputFile.getAbsolutePath(),
                tempOutputFile.getAbsolutePath()
        );
        RuntimeUtil.execForStr(cmdLine);
        if(!tempOutputFile.exists() || tempOutputFile.length() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败");
        }
        // 清理输入文件，保留输出文件供上传使用
        FileUtil.del(tempInputFile);
        return tempOutputFile;
    }
}
