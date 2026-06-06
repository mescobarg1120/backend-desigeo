package com.backend_desigeo.gestion_de_usuarios.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class AuditLogDto {

    private Long id;
    private String eventType;
    private String level;
    private String entityType;
    private String entityId;
    private String actorEmail;
    private String oldValue;
    private String newValue;
    private String description;
    private Integer comunaId;
    private Instant createdAt;
}