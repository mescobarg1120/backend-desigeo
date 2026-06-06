package com.backend_desigeo.gestion_de_usuarios.service;

import com.backend_desigeo.gestion_de_usuarios.dto.AuditLogDto;
import com.backend_desigeo.gestion_de_usuarios.entity.AuditLog;
import com.backend_desigeo.gestion_de_usuarios.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Map<String, Object> getLogs(String level, String eventType, int page, int size) {
        Page<AuditLog> pageResult = auditLogRepository.findWithFilters(
            level, eventType, PageRequest.of(page, size)
        );

        List<AuditLogDto> logs = pageResult.getContent()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("logs", logs);
        response.put("total", pageResult.getTotalElements());
        response.put("page", page);
        response.put("totalPages", pageResult.getTotalPages());
        response.put("kpis", getKpis());
        return response;
    }

    private Map<String, Long> getKpis() {
        Map<String, Long> kpis = new HashMap<>();
        kpis.put("INFO",    auditLogRepository.countByLevel("INFO"));
        kpis.put("WARNING", auditLogRepository.countByLevel("WARNING"));
        kpis.put("ERROR",   auditLogRepository.countByLevel("ERROR"));
        return kpis;
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
            .id(log.getId())
            .eventType(log.getEventType())
            .level(log.getLevel())
            .entityType(log.getEntityType())
            .entityId(log.getEntityId())
            .actorEmail(log.getActorEmail())
            .oldValue(log.getOldValue())
            .newValue(log.getNewValue())
            .description(log.getDescription())
            .comunaId(log.getComunaId())
            .createdAt(log.getCreatedAt())
            .build();
    }
}