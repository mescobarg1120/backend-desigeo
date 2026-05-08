package com.backend_desigeo.gestion_de_usuarios.repository;

import com.backend_desigeo.gestion_de_usuarios.entity.Role;
import com.backend_desigeo.gestion_de_usuarios.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(RoleName roleName);
}