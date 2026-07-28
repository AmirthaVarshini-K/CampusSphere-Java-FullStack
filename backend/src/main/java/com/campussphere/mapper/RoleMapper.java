package com.campussphere.mapper;

import com.campussphere.dto.user.RoleResponse;
import com.campussphere.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setCode(role.getCode().name());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setPermissions(role.getPermissions().stream().map(permission -> permission.getCode()).toList());
        return response;
    }

    public List<RoleResponse> toResponses(Iterable<Role> roles) {
        return roles instanceof List<Role> roleList
                ? roleList.stream().map(this::toResponse).toList()
                : java.util.stream.StreamSupport.stream(roles.spliterator(), false).map(this::toResponse).toList();
    }
}
