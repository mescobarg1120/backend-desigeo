package com.backend_desigeo.desigeo_auth_service.mapper;

import com.backend_desigeo.desigeo_auth_service.dto.UserDTO;
import com.backend_desigeo.desigeo_auth_service.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRut(user.getRut());
        dto.setPhone(user.getPhone());
        dto.setRoleId(user.getRoleId());
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        dto.setActive(user.isActive());
        dto.setComunaId(user.getComunaId());
        return dto;
    }
}