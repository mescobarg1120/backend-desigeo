package com.backend_desigeo.gestion_de_usuarios.dto;

import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
import java.time.Instant;
import java.util.UUID;

public class UserDto {

    private UUID userId;
    private String email;
    private String fullName;
    private String rut;
    private String phone;
    private RoleName roleName;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer comunaId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

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

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getComunaId() {
    return comunaId;
    }

    public void setComunaId(Integer comunaId) {
    this.comunaId = comunaId;
    }
}