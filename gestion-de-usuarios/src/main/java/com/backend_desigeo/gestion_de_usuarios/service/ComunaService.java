package com.backend_desigeo.gestion_de_usuarios.service;

import com.backend_desigeo.gestion_de_usuarios.dto.ComunaCreateDto;
import com.backend_desigeo.gestion_de_usuarios.dto.ComunaDto;
import com.backend_desigeo.gestion_de_usuarios.entity.ComunaEntity;
import com.backend_desigeo.gestion_de_usuarios.repository.ComunaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComunaService {

    private final ComunaRepository comunaRepository;

    public List<ComunaDto> getAllComunas() {
        return comunaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ComunaDto> getComunasByStatus(Boolean isActive) {
        return comunaRepository.findByIsActiveOrderByNombreAsc(isActive)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ComunaDto getComunaById(Integer id) {
        ComunaEntity comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada con id: " + id));
        return toDto(comuna);
    }

    public ComunaDto createComuna(ComunaCreateDto request) {
        if (comunaRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe una comuna con el nombre: " + request.getNombre());
        }

        ComunaEntity comuna = new ComunaEntity();
        comuna.setNombre(request.getNombre());
        comuna.setRegion(request.getRegion());
        comuna.setCodigoIne(request.getCodigoIne());
        comuna.setIsActive(true);
        comuna.setCreatedAt(Instant.now());

        return toDto(comunaRepository.save(comuna));
    }

    public ComunaDto updateComuna(Integer id, ComunaCreateDto request) {
        ComunaEntity comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada con id: " + id));

        // Verificar nombre duplicado solo si cambió
        if (!comuna.getNombre().equals(request.getNombre()) &&
                comunaRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe una comuna con el nombre: " + request.getNombre());
        }

        comuna.setNombre(request.getNombre());
        comuna.setRegion(request.getRegion());
        comuna.setCodigoIne(request.getCodigoIne());

        return toDto(comunaRepository.save(comuna));
    }

    public ComunaDto toggleActive(Integer id) {
        ComunaEntity comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada con id: " + id));

        comuna.setIsActive(!comuna.getIsActive());
        return toDto(comunaRepository.save(comuna));
    }

    public void deleteComuna(Integer id) {
        ComunaEntity comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada con id: " + id));

        Long agentCount = comunaRepository.countAgentsByComunaId(id);
        if (agentCount > 0) {
            throw new RuntimeException(
                "No se puede eliminar la comuna porque tiene " + agentCount + " agente(s) asignado(s)"
            );
        }

        comunaRepository.delete(comuna);
    }

    private ComunaDto toDto(ComunaEntity entity) {
    Long agentCount = comunaRepository.countAgentsByComunaId(entity.getComunaId());
    ComunaDto.ComunaDtoBuilder builder = ComunaDto.builder()
            .comunaId(entity.getComunaId())
            .nombre(entity.getNombre())
            .region(entity.getRegion())
            .codigoIne(entity.getCodigoIne())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .agentCount(agentCount);

    comunaRepository.findAdminByComunaId(entity.getComunaId()).ifPresent(admin -> {
        builder.adminEmail(admin.getEmail());
        builder.adminName(admin.getFullName());
    });

    return builder.build();
}

    
}