package com.backend_desigeo.desigeo_analytics_service.controller;

import com.backend_desigeo.desigeo_analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Dashboard global — solo SUPER_ADMIN
    @GetMapping("/dashboard/global")
    public ResponseEntity<Map<String, Object>> getGlobalDashboard(
            @RequestHeader("X-User-Role") String userRole) {
        if (!"SUPER_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(analyticsService.getGlobalDashboard());
        } catch (Exception e) {
            log.error("Error obteniendo dashboard global: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Dashboard por comuna — ADMIN_MUNICIPAL y SUPER_ADMIN
    @GetMapping("/dashboard/comuna/{comunaId}")
    public ResponseEntity<Map<String, Object>> getDashboardByComuna(
            @PathVariable Integer comunaId,
            @RequestHeader("X-User-Role") String userRole) {
        if (!"SUPER_ADMIN".equals(userRole) && !"ADMIN_MUNICIPAL".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(analyticsService.getDashboardByComuna(comunaId));
        } catch (Exception e) {
            log.error("Error obteniendo dashboard comuna {}: {}", comunaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lista de reportes con filtros — todos los roles municipales
    @GetMapping("/reportes")
    public ResponseEntity<Map<String, Object>> getReportes(
            @RequestParam(required = false) Integer comunaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader(value = "X-User-Comuna", required = false) String userComunaHeader) {

        // MUNICIPAL_OFFICER y ADMIN_MUNICIPAL solo ven su comuna
        if (!"SUPER_ADMIN".equals(userRole)) {
            if (userComunaHeader != null) {
                comunaId = Integer.parseInt(userComunaHeader);
            }
        }

        try {
            return ResponseEntity.ok(
                analyticsService.getReportes(comunaId, status, category, priority, desde, hasta, page, size)
            );
        } catch (Exception e) {
            log.error("Error obteniendo reportes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Categorías disponibles — para poblar filtros en el frontend
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> getCategorias(
            @RequestHeader("X-User-Role") String userRole) {
        try {
            return ResponseEntity.ok(analyticsService.getCategorias());
        } catch (Exception e) {
            log.error("Error obteniendo categorías: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}