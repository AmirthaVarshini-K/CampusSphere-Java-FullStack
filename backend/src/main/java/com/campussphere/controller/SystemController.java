package com.campussphere.controller;

import com.campussphere.constants.ApplicationConstants;
import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.ApplicationInfoDto;
import com.campussphere.service.SystemService;
import com.campussphere.util.ApiResponseFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponseFactory.success(ApplicationConstants.SYSTEM_HEALTH_MESSAGE);
    }

    @GetMapping("/info")
    public ApiResponse<ApplicationInfoDto> info() {
        return ApiResponseFactory.success(systemService.getApplicationInfo());
    }
}
