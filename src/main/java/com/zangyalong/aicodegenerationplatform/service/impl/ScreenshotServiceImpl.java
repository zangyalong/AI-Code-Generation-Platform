package com.zangyalong.aicodegenerationplatform.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.zangyalong.aicodegenerationplatform.exception.ErrorCode;
import com.zangyalong.aicodegenerationplatform.exception.ThrowUtils;
import com.zangyalong.aicodegenerationplatform.manager.CosManager;
import com.zangyalong.aicodegenerationplatform.service.ScreenshotService;
import com.zangyalong.aicodegenerationplatform.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);
        // 1. 生成本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        try {
            // 2. 上传到对象存储
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;
        } finally {
            // 3. 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    @Override
    public String deleteScreenshotWithCos(String cover) {
        if (StrUtil.isBlank(cover)) {
            log.warn("封面URL为空，无需删除");
            return "封面URL为空，无需删除";
        }
        
        try {
            // 从URL中提取COS对象键
            String cosKey = extractCosKeyFromUrl(cover);
            if (StrUtil.isBlank(cosKey)) {
                log.warn("无法从URL提取COS对象键: {}", cover);
                return "无法从URL提取COS对象键";
            }
            
            // 删除COS对象
            cosManager.deleteObject(cosKey);
            log.info("成功删除COS截图文件: {}", cosKey);
            return "删除成功";
            
        } catch (Exception e) {
            log.error("删除COS截图文件失败: {}", cover, e);
            return "删除失败: " + e.getMessage();
        }
    }
    
    /**
     * 从URL中提取COS对象键
     * 
     * @param url COS访问URL
     * @return COS对象键，失败返回null
     */
    private String extractCosKeyFromUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        
        try {
            // 处理不同格式的URL
            // 1. 直接的对象键格式: /screenshots/2025/07/31/filename.jpg
            if (url.startsWith("/screenshots/")) {
                return url;
            }
            
            // 2. 完整URL格式: https://domain.com/bucket/screenshots/2025/07/31/filename.jpg
            if (url.contains("/screenshots/")) {
                int index = url.indexOf("/screenshots/");
                return url.substring(index);
            }
            
            // 3. 其他格式，尝试解析路径部分
            String[] parts = url.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("screenshots".equals(parts[i]) && i + 3 < parts.length) {
                    // 重新构建对象键: /screenshots/yyyy/MM/dd/filename
                    StringBuilder keyBuilder = new StringBuilder("/screenshots");
                    for (int j = i + 1; j < parts.length; j++) {
                        keyBuilder.append("/").append(parts[j]);
                    }
                    return keyBuilder.toString();
                }
            }
            
            log.warn("无法从URL解析COS对象键: {}", url);
            return null;
            
        } catch (Exception e) {
            log.error("解析COS对象键时发生异常: {}", url, e);
            return null;
        }
    }

    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成 COS 对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/07/31/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
