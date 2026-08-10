package com.campussphere.controller;

import com.campussphere.constants.ApplicationConstants;
import com.campussphere.dto.ApiResponse;
import com.campussphere.util.ApiResponseFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponseFactory.success(ApplicationConstants.SYSTEM_HEALTH_MESSAGE);
    }
}
