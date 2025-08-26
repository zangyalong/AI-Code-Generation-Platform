package com.zangyalong.aicodegenerationplatform.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageResource;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.enums.ImageCategoryEnum;
import com.zangyalong.aicodegenerationplatform.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model}")
    private String imageModel;

    @Resource
    private CosManager cosManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细")String description) {
        List<ImageResource> logoList = new ArrayList<>();

        try {
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(logoPrompt)
                    .size("768*768")
                    .n(1)
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            if(result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for(Map<String, String> imageResult : results) {
                    String imageUrl = imageResult.get("url");

                    // 上传到自己的oss中，防止连接过期
                    URL url = new URL(imageUrl);
                    String CosUrl = null;
                    try (InputStream inputStream = url.openStream()) {
                        String path = url.getPath(); // /images/logo123.png
                        String fileName = path.substring(path.lastIndexOf("/") + 1); // logo123.png

                        String key = String.format("/logo/%s/%s", RandomUtil.randomString(5), fileName);

                        // 根据原始文件名后缀创建临时文件
                        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : ".png";
                        File tempFile = File.createTempFile("logo-", suffix);

                        FileUtils.copyInputStreamToFile(inputStream, tempFile);
                        CosUrl = cosManager.uploadFile(key, tempFile);

                        FileUtil.del(tempFile);
                    }

                    if(StrUtil.isNotBlank(CosUrl)) {
                        logoList.add(ImageResource.builder()
                                .category(ImageCategoryEnum.LOGO)
                                .description(description)
                                .url(CosUrl)
                                .build());
                    }
                }
            }

        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }
}
