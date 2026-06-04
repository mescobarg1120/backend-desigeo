package com.backend_desigeo.gestion_de_usuarios.repository;

import com.backend_desigeo.gestion_de_usuarios.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByComunaId(Integer comunaId);
}