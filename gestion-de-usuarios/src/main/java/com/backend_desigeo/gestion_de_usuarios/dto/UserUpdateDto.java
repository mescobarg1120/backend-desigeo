package com.backend_desigeo.gestion_de_usuarios.dto;

import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
import jakarta.validation.constraints.Email;

public class UserUpdateDto {

    @Email(message = "Email should be valid")
    private String email;

    private String fullName;

    private RoleName roleName;

    private Boolean active;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}