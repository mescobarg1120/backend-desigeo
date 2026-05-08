package com.backend_desigeo.gestion_de_usuarios.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "userid", nullable = false)
    private UUID userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "passwordhash", nullable = false)
    private String passwordHash;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "roleid")
    private Integer roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleid", insertable = false, updatable = false)
    private Role role;

    @Column(name = "comunaid")
    private Integer comunaId;

    @Column(name = "department")
    private String department;

    @Column(name = "geographicarea")
    private String geographicArea;

    @Column(name = "notificationprefs", nullable = false)
    private String notificationPrefs;

    @Column(name = "isactive")
    private Boolean active = Boolean.TRUE;

    @Column(name = "failedlogincount", nullable = false)
    private Integer failedLoginCount = 0;

    @Column(name = "lockeduntil")
    private LocalDateTime lockedUntil;

    @Column(name = "lastloginat")
    private LocalDateTime lastLoginAt;

    @Column(name = "createdat")
    private Instant createdAt;

    @Column(name = "updatedat")
    private Instant updatedAt;

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

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}