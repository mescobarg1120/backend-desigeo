package com.backend_desigeo.gestion_de_usuarios.controller;

import com.backend_desigeo.gestion_de_usuarios.dto.ComunaCreateDto;
import com.backend_desigeo.gestion_de_usuarios.dto.ComunaDto;
import com.backend_desigeo.gestion_de_usuarios.service.ComunaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comunas")
@RequiredArgsConstructor
public class ComunaController {

    private final ComunaService comunaService;

    // Listar todas — accesible para todos los roles autenticados (necesario para selects en frontend)
    @GetMapping
    public ResponseEntity<List<ComunaDto>> getAllComunas(
            @RequestParam(required = false) Boolean isActive) {
        if (isActive != null) {
            return ResponseEntity.ok(comunaService.getComunasByStatus(isActive));
        }
        return ResponseEntity.ok(comunaService.getAllComunas());
    }

    // Detalle por id
    @GetMapping("/{id}")
    public ResponseEntity<ComunaDto> getComunaById(@PathVariable Integer id) {
        return ResponseEntity.ok(comunaService.getComunaById(id));
    }

    // Crear — solo SUPER_ADMIN (validado en gateway)
    @PostMapping
    public ResponseEntity<ComunaDto> createComuna(
            @Valid @RequestBody ComunaCreateDto request,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"SUPER_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comunaService.createComuna(request));
    }

    // Editar — solo SUPER_ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<ComunaDto> updateComuna(
            @PathVariable Integer id,
            @Valid @RequestBody ComunaCreateDto request,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"SUPER_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(comunaService.updateComuna(id, request));
    }

    // Activar/desactivar — solo SUPER_ADMIN
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ComunaDto> toggleActive(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"SUPER_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(comunaService.toggleActive(id));
    }

    // Eliminar — solo SUPER_ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComuna(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"SUPER_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        comunaService.deleteComuna(id);
        return ResponseEntity.noContent().build();
    }
}