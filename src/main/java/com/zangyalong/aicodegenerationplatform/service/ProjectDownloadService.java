package com.zangyalong.aicodegenerationplatform.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
