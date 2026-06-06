package com.backend_desigeo.desigeo_auth_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;



@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "userid", nullable = false)
    private UUID userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "passwordhash")
    private String passwordHash;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "rut")
    private String rut;

    @Column(name = "phone")
    private String phone;

    @Column(name = "roleid")
    private Integer roleId;

    @Column(name = "comunaid")
    private Integer comunaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleid", insertable = false, updatable = false)
    private Role role;

    @Column(name = "isactive")
    private Boolean active = Boolean.TRUE;

    @Column(name = "failedlogincount")
    private Integer failedLoginCount;

    @Column(name = "lockeduntil")
    private Instant lockedUntil;

    @Column(name = "lastloginat")
    private Instant lastLoginAt;

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Integer getComunaId() {
    return comunaId;
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

    public Integer getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(Integer failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
    
    public void setComunaId(Integer comunaId) {
    this.comunaId = comunaId;
    }
}
