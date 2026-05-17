package com.backend_desigeo.desigeo_auth_service.repository;

import com.backend_desigeo.desigeo_auth_service.entity.PasswordReset;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    @Query("SELECT pr FROM PasswordReset pr WHERE pr.userId = :userId AND pr.used = false AND pr.expiresAt > :now ORDER BY pr.createdAt DESC LIMIT 1")
    Optional<PasswordReset> findLastActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    Optional<PasswordReset> findByTokenAndUsedFalse(String token);
}
