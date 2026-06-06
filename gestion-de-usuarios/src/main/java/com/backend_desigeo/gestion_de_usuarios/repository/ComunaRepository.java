package com.backend_desigeo.gestion_de_usuarios.repository;
 
import com.backend_desigeo.gestion_de_usuarios.entity.ComunaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.backend_desigeo.gestion_de_usuarios.entity.User;
import java.util.Optional;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ComunaRepository extends JpaRepository<ComunaEntity, Integer> {
 
    List<ComunaEntity> findAllByOrderByNombreAsc();
 
    List<ComunaEntity> findByIsActiveOrderByNombreAsc(Boolean isActive);
 
    Optional<ComunaEntity> findByNombre(String nombre);
 
    boolean existsByNombre(String nombre);
 
    // Conteo de agentes (usuarios con comunaId asignada)
    @Query("SELECT COUNT(u) FROM User u WHERE u.comunaId = :comunaId")
    Long countAgentsByComunaId(@Param("comunaId") Integer comunaId);

    @Query("SELECT u FROM User u WHERE u.comunaId = :comunaId AND u.roleId = 3")
    Optional<User> findAdminByComunaId(@Param("comunaId") Integer comunaId);
}
