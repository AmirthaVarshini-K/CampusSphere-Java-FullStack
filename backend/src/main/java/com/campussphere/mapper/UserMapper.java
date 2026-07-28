package com.campussphere.mapper;

import com.campussphere.dto.user.UserProfileResponse;
import com.campussphere.dto.user.UserSummaryResponse;
import com.campussphere.entity.Role;
import com.campussphere.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserProfileResponse toProfileResponse(User user, List<Role> roles) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(buildFullName(user));
        response.setEmail(user.getEmail());
        response.setRegisterNumber(user.getRegisterNumber());
        response.setEmployeeId(user.getEmployeeId());
        response.setDepartment(user.getDepartment());
        response.setAcademicYear(user.getAcademicYear());
        response.setSection(user.getSection());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setStatus(user.getStatus());
        response.setTermsAccepted(user.isTermsAccepted());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setPasswordChangedAt(user.getPasswordChangedAt());
        response.setRoles(roleMapper.toResponses(roles));
        return response;
    }

    public UserSummaryResponse toSummaryResponse(User user, List<Role> roles) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setDisplayName(buildFullName(user));
        response.setEmail(user.getEmail());
        response.setPrincipalIdentifier(user.getRegisterNumber() != null ? user.getRegisterNumber() : user.getEmployeeId());
        response.setRole(roles.isEmpty() ? null : roles.get(0).getCode().name());
        response.setStatus(user.getStatus());
        return response;
    }

    private String buildFullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
