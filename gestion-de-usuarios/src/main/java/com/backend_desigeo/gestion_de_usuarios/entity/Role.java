package com.backend_desigeo.gestion_de_usuarios.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleid")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolename", nullable = false, unique = true)
    private RoleName roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "isactive")
    private Boolean active = Boolean.TRUE;


    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}