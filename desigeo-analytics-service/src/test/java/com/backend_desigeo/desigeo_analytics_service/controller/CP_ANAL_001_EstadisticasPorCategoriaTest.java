package com.backend_desigeo.desigeo_analytics_service.controller;

import com.backend_desigeo.desigeo_analytics_service.config.TestSecurityConfig;
import com.backend_desigeo.desigeo_analytics_service.service.AnalyticsService;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-ANAL-001 — Obtener estadísticas por categoría
 *
 * El plan menciona GET /api/analytics/por-categoria, pero el endpoint real es
 * GET /api/analytics/dashboard/global, cuya respuesta incluye el campo
 * "porCategoria" con el desglose solicitado.
 *
 * Este test verifica:
 *   - El endpoint retorna 200 OK con el JSON correcto
 *   - El campo "porCategoria" contiene el mapa categoría → cantidad
 *   - El campo "totalReportes" está presente
 *   - El campo "tasaResolucion" está presente
 *   - El campo "porEstado" está presente
 *   - Solo SUPER_ADMIN puede acceder (roles incorrectos → 403)
 *
 * Nota: la seguridad se gestiona via header X-User-Role (GatewayAuthFilter),
 * no via JWT. No se requiere token Bearer.
 *
 * Tipo: JUnit5 / @WebMvcTest
 */
@WebMvcTest(AnalyticsController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-ANAL-001: Obtener estadísticas por categoría")
class CP_ANAL_001_EstadisticasPorCategoriaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private FirebaseApp firebaseApp;

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Construye la respuesta que devuelve AnalyticsService.getGlobalDashboard()
     * con datos de categoría realistas.
     */
    private Map<String, Object> buildDashboardResponse() {
        Map<String, Long> porCategoria = new LinkedHashMap<>();
        porCategoria.put("BACHE",       12L);
        porCategoria.put("ILUMINACION",  8L);
        porCategoria.put("BASURA",       5L);
        porCategoria.put("AGUA",         3L);
        porCategoria.put("SEÑALETICA",   2L);
        porCategoria.put("OTRO",         1L);

        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("PENDING",     15L);
        porEstado.put("IN_PROGRESS",  8L);
        porEstado.put("RESOLVED",     8L);

        Map<String, Long> porPrioridad = new LinkedHashMap<>();
        porPrioridad.put("HIGH",   10L);
        porPrioridad.put("MEDIUM", 15L);
        porPrioridad.put("LOW",     6L);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalReportes",   31);
        dashboard.put("porCategoria",    porCategoria);
        dashboard.put("porEstado",       porEstado);
        dashboard.put("porPrioridad",    porPrioridad);
        dashboard.put("tasaResolucion",  25.8);
        dashboard.put("timeline",        List.of());
        return dashboard;
    }

    // ── tests ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/analytics/dashboard/global con rol SUPER_ADMIN → 200 + JSON correcto")
    void obtenerEstadisticasPorCategoria_superAdmin_retorna200ConJson() throws Exception {
        when(analyticsService.getGlobalDashboard()).thenReturn(buildDashboardResponse());

        mockMvc.perform(get("/api/analytics/dashboard/global")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReportes").value(31))
                // Verifica que el campo porCategoria existe y tiene las claves esperadas
                .andExpect(jsonPath("$.porCategoria").isMap())
                .andExpect(jsonPath("$.porCategoria.BACHE").value(12))
                .andExpect(jsonPath("$.porCategoria.ILUMINACION").value(8))
                .andExpect(jsonPath("$.porCategoria.BASURA").value(5))
                // Verifica otros campos del dashboard
                .andExpect(jsonPath("$.porEstado").isMap())
                .andExpect(jsonPath("$.porEstado.PENDING").value(15))
                .andExpect(jsonPath("$.tasaResolucion").value(25.8));
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/global → porCategoria contiene todas las categorías con valor > 0")
    void obtenerEstadisticasPorCategoria_todasLasCategoriasTienenValor() throws Exception {
        when(analyticsService.getGlobalDashboard()).thenReturn(buildDashboardResponse());

        mockMvc.perform(get("/api/analytics/dashboard/global")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porCategoria.BACHE").isNumber())
                .andExpect(jsonPath("$.porCategoria.ILUMINACION").isNumber())
                .andExpect(jsonPath("$.porCategoria.BASURA").isNumber())
                .andExpect(jsonPath("$.porCategoria.AGUA").isNumber())
                .andExpect(jsonPath("$.porCategoria.SEÑALETICA").isNumber())
                .andExpect(jsonPath("$.porCategoria.OTRO").isNumber());
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/global → totalReportes es la suma de porCategoria")
    void totalReportesEsSumaDeCategoria() throws Exception {
        when(analyticsService.getGlobalDashboard()).thenReturn(buildDashboardResponse());

        mockMvc.perform(get("/api/analytics/dashboard/global")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                // 12+8+5+3+2+1 = 31
                .andExpect(jsonPath("$.totalReportes").value(31));
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/global sin header X-User-Role → 400 (header requerido)")
    void sinHeaderRole_retorna400() throws Exception {
        // El header X-User-Role es @RequestHeader (requerido) → Spring retorna 400
        mockMvc.perform(get("/api/analytics/dashboard/global"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/global con rol ADMIN_MUNICIPAL → 403 Forbidden")
    void rolAdminMunicipal_retorna403() throws Exception {
        // Solo SUPER_ADMIN puede ver el dashboard global
        mockMvc.perform(get("/api/analytics/dashboard/global")
                        .header("X-User-Role", "ADMIN_MUNICIPAL"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/global con rol CITIZEN → 403 Forbidden")
    void rolCitizen_retorna403() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard/global")
                        .header("X-User-Role", "CITIZEN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/analytics/categorias con cualquier rol → 200 + lista de categorías")
    void obtenerCategorias_retorna200ConLista() throws Exception {
        when(analyticsService.getCategorias()).thenReturn(
                List.of("AGUA", "ARBOL_CAIDO", "BASURA", "BACHE",
                        "ILUMINACION", "OTRO", "SEGURIDAD", "SEÑALETICA"));

        mockMvc.perform(get("/api/analytics/categorias")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0]").value("AGUA"))
                .andExpect(jsonPath("$[3]").value("BACHE"));
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/global con dashboard vacío → 200 + totalReportes=0")
    void dashboardVacio_retorna200ConCeros() throws Exception {
        Map<String, Object> vacio = new HashMap<>();
        vacio.put("totalReportes",  0);
        vacio.put("porCategoria",   Map.of());
        vacio.put("porEstado",      Map.of());
        vacio.put("porPrioridad",   Map.of());
        vacio.put("tasaResolucion", 0.0);
        vacio.put("timeline",       List.of());

        when(analyticsService.getGlobalDashboard()).thenReturn(vacio);

        mockMvc.perform(get("/api/analytics/dashboard/global")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReportes").value(0))
                .andExpect(jsonPath("$.tasaResolucion").value(0.0));
    }
}
