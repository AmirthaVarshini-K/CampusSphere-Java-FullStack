package com.campussphere.service;

import com.campussphere.dto.user.UserCreateRequest;
import com.campussphere.dto.user.UserProfileResponse;
import com.campussphere.dto.user.UserSummaryResponse;
import com.campussphere.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserProfileResponse getCurrentUser(String currentUserEmail);

    UserProfileResponse updateCurrentUser(String currentUserEmail, UserUpdateRequest request);

    List<UserSummaryResponse> listUsers();

    UserProfileResponse getUser(Long id);

    UserProfileResponse createUser(UserCreateRequest request);

    UserProfileResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}
