package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.user.UserCreateRequest;
import com.campussphere.dto.user.UserProfileResponse;
import com.campussphere.dto.user.UserSummaryResponse;
import com.campussphere.dto.user.UserUpdateRequest;
import com.campussphere.service.UserService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        return ApiResponseFactory.success("Current profile retrieved successfully.", userService.getCurrentUser(currentUserEmail()));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return ApiResponseFactory.success("Profile updated successfully.", userService.updateCurrentUser(currentUserEmail(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<List<UserSummaryResponse>> listUsers() {
        return ApiResponseFactory.success("Users retrieved successfully.", userService.listUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserProfileResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponseFactory.success("User created successfully.", userService.createUser(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserProfileResponse> getUser(@PathVariable Long id) {
        return ApiResponseFactory.success("User retrieved successfully.", userService.getUser(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<UserProfileResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponseFactory.success("User updated successfully.", userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponseFactory.success("User deleted successfully.", "User archived successfully.");
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
