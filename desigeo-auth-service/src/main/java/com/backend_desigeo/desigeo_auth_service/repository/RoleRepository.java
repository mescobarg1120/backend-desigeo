package com.backend_desigeo.desigeo_auth_service.repository;

import com.backend_desigeo.desigeo_auth_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
