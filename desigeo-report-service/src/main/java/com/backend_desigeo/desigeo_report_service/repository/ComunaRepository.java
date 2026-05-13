package com.backend_desigeo.desigeo_report_service.repository;

import com.backend_desigeo.desigeo_report_service.entity.ComunaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ComunaRepository extends JpaRepository<ComunaEntity, Integer> {
    Optional<ComunaEntity> findByNombreIgnoreCase(String nombre);
}
