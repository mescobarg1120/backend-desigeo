package com.backend_desigeo.desigeo_analytics_service.controller;

import com.backend_desigeo.desigeo_analytics_service.config.TestSecurityConfig;
import com.backend_desigeo.desigeo_analytics_service.service.AnalyticsService;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-ANAL-002 — Filtrar por rango de fechas / filtros disponibles
 *
 * El plan menciona GET /api/analytics?desde=2026-01-01&hasta=2026-06-02,
 * pero el endpoint real es GET /api/analytics/reportes con parámetros
 * status, category, priority, comunaId, page y size. Los parámetros
 * "desde" y "hasta" no están implementados en el controller actual.
 *
 * Este test cubre el comportamiento real del endpoint de filtrado:
 *   - Filtro por status (equivalente funcional al rango temporal)
 *   - Filtro por category
 *   - Filtro por priority
 *   - Paginación (page/size)
 *   - Restricción por rol (ADMIN_MUNICIPAL solo ve su comuna)
 *
 * Nota: si se requiere filtrado por fecha en el futuro, el plan deberá
 * actualizarse para agregar los parámetros "desde" y "hasta" al controller
 * y al AnalyticsService. Este test documenta el comportamiento actual.
 *
 * Tipo: JUnit5 / @WebMvcTest
 */
@WebMvcTest(AnalyticsController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-ANAL-002: Filtrar por rango de fechas / filtros en GET /api/analytics/reportes")
class CP_ANAL_002_FiltrarPorFechasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private FirebaseApp firebaseApp;

    // ── helpers ────────────────────────────────────────────────────────────

    private Map<String, Object> buildReportesPage(int total, int page, int size,
                                                   List<Map<String, Object>> reportes) {
        Map<String, Object> result = new HashMap<>();
        result.put("reportes",   reportes);
        result.put("total",      total);
        result.put("page",       page);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    private Map<String, Object> buildReporte(String id, String category,
                                              String status, String createdAt) {
        Map<String, Object> r = new HashMap<>();
        r.put("reportId",    id);
        r.put("description", "Denuncia de prueba " + id);
        r.put("category",    category);
        r.put("status",      status);
        r.put("priority",    "MEDIUM");
        r.put("createdAt",   createdAt);
        return r;
    }

    // ── tests ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/analytics/reportes sin filtros (SUPER_ADMIN) → 200 + lista paginada")
    void listarReportesSinFiltros_superAdmin_retorna200() throws Exception {
        List<Map<String, Object>> lista = List.of(
                buildReporte("r001", "BACHE",      "PENDING",     "2026-01-15T10:00:00Z"),
                buildReporte("r002", "ILUMINACION","IN_PROGRESS", "2026-02-20T12:00:00Z"),
                buildReporte("r003", "BASURA",     "RESOLVED",    "2026-03-05T09:00:00Z")
        );
        Map<String, Object> response = buildReportesPage(3, 0, 20, lista);

        when(analyticsService.getReportes(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.reportes").isArray())
                .andExpect(jsonPath("$.reportes.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/analytics/reportes?status=PENDING → 200 + solo reportes pendientes")
    void filtrarPorEstadoPending_retorna200() throws Exception {
        List<Map<String, Object>> lista = List.of(
                buildReporte("r001", "BACHE",     "PENDING", "2026-01-15T10:00:00Z"),
                buildReporte("r004", "AGUA",      "PENDING", "2026-03-10T11:00:00Z"),
                buildReporte("r005", "SEÑALETICA", "PENDING", "2026-04-01T08:00:00Z")
        );
        Map<String, Object> response = buildReportesPage(3, 0, 20, lista);

        when(analyticsService.getReportes(isNull(), eq("PENDING"), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.reportes[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/analytics/reportes?desde=2026-01-01&hasta=2026-06-02 → 200 (BUG-04 corregido)")
    void filtrarPorRangoDeFechas_retorna200() throws Exception {
        // BUG-04 fix: los parámetros desde/hasta ahora existen en el controller y servicio
        List<Map<String, Object>> lista = List.of(
                buildReporte("r010", "BACHE",      "RESOLVED", "2026-01-20T10:00:00Z"),
                buildReporte("r011", "BASURA",     "RESOLVED", "2026-02-14T14:00:00Z"),
                buildReporte("r012", "ILUMINACION","RESOLVED", "2026-05-30T16:00:00Z")
        );
        Map<String, Object> response = buildReportesPage(3, 0, 20, lista);

        when(analyticsService.getReportes(isNull(), isNull(), isNull(), isNull(),
                eq("2026-01-01"), eq("2026-06-02"), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-06-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.reportes[0].createdAt").value("2026-01-20T10:00:00Z"))
                .andExpect(jsonPath("$.reportes[2].createdAt").value("2026-05-30T16:00:00Z"));
    }

    @Test
    @DisplayName("GET /api/analytics/reportes?category=BACHE&status=IN_PROGRESS → 200 + filtros combinados")
    void filtrarPorCategoriaYEstado_retorna200() throws Exception {
        List<Map<String, Object>> lista = List.of(
                buildReporte("r020", "BACHE", "IN_PROGRESS", "2026-02-01T10:00:00Z")
        );
        Map<String, Object> response = buildReportesPage(1, 0, 20, lista);

        when(analyticsService.getReportes(isNull(), eq("IN_PROGRESS"), eq("BACHE"),
                isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN")
                        .param("status",   "IN_PROGRESS")
                        .param("category", "BACHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.reportes[0].category").value("BACHE"))
                .andExpect(jsonPath("$.reportes[0].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("GET /api/analytics/reportes con paginación page=1&size=2 → 200 + página correcta")
    void paginacion_retorna200PaginaCorrecta() throws Exception {
        List<Map<String, Object>> lista = List.of(
                buildReporte("r003", "BASURA",    "PENDING", "2026-03-05T09:00:00Z"),
                buildReporte("r004", "SEÑALETICA", "PENDING", "2026-04-01T08:00:00Z")
        );
        Map<String, Object> response = buildReportesPage(5, 1, 2, lista);

        when(analyticsService.getReportes(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(1), eq(2)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.reportes.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/analytics/reportes como ADMIN_MUNICIPAL → 200 + restringido a su comuna")
    void adminMunicipal_vistaRestringidaPorComuna_retorna200() throws Exception {
        List<Map<String, Object>> lista = List.of(
                buildReporte("r030", "BACHE", "PENDING", "2026-01-10T10:00:00Z")
        );
        Map<String, Object> response = buildReportesPage(1, 0, 20, lista);

        when(analyticsService.getReportes(eq(5), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role",   "ADMIN_MUNICIPAL")
                        .header("X-User-Comuna", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));

        verify(analyticsService).getReportes(eq(5), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/analytics/reportes sin resultados → 200 + lista vacía")
    void sinResultados_retorna200ListaVacia() throws Exception {
        Map<String, Object> vacio = buildReportesPage(0, 0, 20, List.of());

        when(analyticsService.getReportes(any(), any(), any(), any(),
                any(), any(), anyInt(), anyInt()))
                .thenReturn(vacio);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN")
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.reportes").isEmpty());
    }

    @ParameterizedTest(name = "priority={0}")
    @CsvSource({"HIGH", "MEDIUM", "LOW", "CRITICAL"})
    @DisplayName("GET /api/analytics/reportes?priority=... → 200 para todas las prioridades")
    void filtrarPorPrioridad_todasLasPrioridades_retorna200(String priority) throws Exception {
        Map<String, Object> response = buildReportesPage(1, 0, 20, List.of(
                buildReporte("r-p", "BACHE", "PENDING", "2026-01-01T00:00:00Z")
        ));

        when(analyticsService.getReportes(isNull(), isNull(), isNull(), eq(priority),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/analytics/reportes")
                        .header("X-User-Role", "SUPER_ADMIN")
                        .param("priority", priority))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard/comuna/{id} con ADMIN_MUNICIPAL → 200 + desglose")
    void dashboardPorComuna_adminMunicipal_retorna200() throws Exception {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalReportes",  8);
        dashboard.put("porCategoria",   Map.of("BACHE", 5L, "BASURA", 3L));
        dashboard.put("porEstado",      Map.of("PENDING", 4L, "RESOLVED", 4L));
        dashboard.put("tasaResolucion", 50.0);
        dashboard.put("timeline",       List.of());

        when(analyticsService.getDashboardByComuna(5)).thenReturn(dashboard);

        mockMvc.perform(get("/api/analytics/dashboard/comuna/{id}", 5)
                        .header("X-User-Role", "ADMIN_MUNICIPAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReportes").value(8))
                .andExpect(jsonPath("$.porCategoria.BACHE").value(5))
                .andExpect(jsonPath("$.tasaResolucion").value(50.0));
    }
}
