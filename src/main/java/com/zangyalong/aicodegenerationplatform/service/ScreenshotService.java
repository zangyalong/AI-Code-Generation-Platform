package com.zangyalong.aicodegenerationplatform.service;

public interface ScreenshotService {

    public String generateAndUploadScreenshot(String webUrl);

    public String deleteScreenshotWithCos(String cover);
}
