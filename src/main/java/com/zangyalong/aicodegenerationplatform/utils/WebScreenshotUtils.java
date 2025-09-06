package com.zangyalong.aicodegenerationplatform.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.zangyalong.aicodegenerationplatform.exception.BusinessException;
import com.zangyalong.aicodegenerationplatform.exception.ErrorCode;
import com.zangyalong.aicodegenerationplatform.exception.ThrowUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenshotUtils {

    // 通过ThreadLocal隔离不同线程的WebDriver，解决并发下的WebDriver串用问题
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;

        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            driver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            driverThreadLocal.set(driver);
        }
        return driver;
    }

    public static void cleanupTempFiles() {
        cleanupTempFiles(1); // 默认删除1天前的文件
    }

    /**
     * 清理过期的临时截图文件
     * 
     * @param daysToKeep 保留多少天内的文件，超过此天数的文件将被删除
     */
    public static void cleanupTempFiles(int daysToKeep) {
        String tempPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots";
        File tempDir = new File(tempPath);
        
        if (!tempDir.exists()) {
            log.info("临时截图目录不存在: {}", tempPath);
            return;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
        int deletedCount = 0;
        int totalSize = 0;
        
        try {
            log.info("开始清理临时截图文件，目录: {}，保留{}天内的文件", tempPath, daysToKeep);
            
            File[] subdirs = tempDir.listFiles();
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.isDirectory()) {
                        if (shouldDeleteDirectory(subdir, cutoffTime)) {
                            totalSize += calculateDirectorySize(subdir);
                            if (FileUtil.del(subdir)) {
                                deletedCount++;
                                log.debug("删除过期目录: {}", subdir.getPath());
                            }
                        }
                    }
                }
            }
            
            log.info("清理完成，删除了{}个目录，释放了{}KB空间", deletedCount, totalSize / 1024);
        } catch (Exception e) {
            log.error("清理临时截图文件时发生异常", e);
        }
    }
    
    /**
     * 判断目录是否应该被删除
     * 
     * @param dir 目录
     * @param cutoffTime 截止时间戳
     * @return true表示应该删除
     */
    private static boolean shouldDeleteDirectory(File dir, long cutoffTime) {
        // 检查目录的最后修改时间
        if (dir.lastModified() < cutoffTime) {
            return true;
        }
        
        // 检查目录中所有文件的修改时间
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() >= cutoffTime) {
                    return false; // 有文件还在保留期内
                }
            }
        }
        
        return true;
    }
    
    /**
     * 计算目录大小
     * 
     * @param dir 目录
     * @return 目录大小（字节）
     */
    private static long calculateDirectorySize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                }
            }
        }
        return size;
    }

    @PreDestroy
    public void destroy() {
        driverThreadLocal.remove();
        getDriver().quit();
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            
            // 指定Chrome浏览器的路径
            options.setBinary("/usr/bin/google-chrome");
            
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 禁用图片加载（提升性能）
            options.addArguments("--disable-images");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.7339.80 Safari/537.36");
            
            // 手动设置ChromeDriver路径，完全避免WebDriverManager
            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
            
            // 创建驱动（不使用WebDriverManager）
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            
            log.info("Chrome浏览器驱动初始化成功");
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败: " + e.getMessage());
        }
    }

    /**
     * 保存图片到文件
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        // 压缩图片质量（0.1 = 10% 质量）
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }
        try {
            WebDriver driver = getDriver();
            // 创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            // 图片后缀
            final String IMAGE_SUFFIX = ".png";
            // 原始截图文件路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
            // 访问网页
            driver.get(webUrl);
            // 等待页面加载完成
            waitForPageLoad(driver);
            // 截图
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功: {}", imageSavePath);
            // 压缩图片
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功: {}", compressedImagePath);
            // 删除原始图片，只保留压缩图片
            FileUtil.del(imageSavePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            return null;
        }
    }

}

