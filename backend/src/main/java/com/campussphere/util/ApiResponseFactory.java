package com.campussphere.util;

import com.campussphere.constants.ApplicationConstants;
import com.campussphere.dto.ApiResponse;

import java.util.List;
import java.util.Map;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(ApplicationConstants.DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, List<String>> errors) {
        return ApiResponse.error(message, errors);
    }
}
