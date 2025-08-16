package com.zangyalong.aicodegenerationplatform.controller;

import com.zangyalong.aicodegenerationplatform.common.BaseResponse;
import com.zangyalong.aicodegenerationplatform.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.prefs.BackingStoreException;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}

