package com.backend_desigeo.gestion_de_usuarios.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "level", nullable = false)
    private String level;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "actor_email")
    private String actorEmail;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "description")
    private String description;

    @Column(name = "comuna_id")
    private Integer comunaId;

    @Column(name = "created_at")
    private Instant createdAt;
}
