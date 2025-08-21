package com.zangyalong.aicodegenerationplatform.timer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.zangyalong.aicodegenerationplatform.utils.WebScreenshotUtils;

@Configuration
@EnableScheduling
@Slf4j
public class ScreenshotTimer {

    /**
     * 每天凌晨2点清理过期的临时截图文件
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempScreenshots() {
        log.info("开始定时清理过期的临时截图文件");
        try {
            WebScreenshotUtils.cleanupTempFiles();
            log.info("定时清理临时截图文件完成");
        } catch (Exception e) {
            log.error("定时清理临时截图文件失败", e);
        }
    }
}

