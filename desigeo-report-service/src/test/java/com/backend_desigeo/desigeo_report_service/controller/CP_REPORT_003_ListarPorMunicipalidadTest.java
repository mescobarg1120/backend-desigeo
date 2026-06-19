package com.backend_desigeo.desigeo_report_service.controller;

import com.backend_desigeo.desigeo_report_service.config.TestSecurityConfig;
import com.backend_desigeo.desigeo_report_service.dto.response.ReportListResponse;
import com.backend_desigeo.desigeo_report_service.dto.response.ReportSummaryResponse;
import com.backend_desigeo.desigeo_report_service.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-REPORT-003 — Listar denuncias por municipalidad / filtros
 * GET /api/reports?category=...&status=... → 200 + lista filtrada
 *
 * Nota: el endpoint real usa parámetros category, status, priority, userId —
 * no existe parámetro "municipalidad". Este test cubre los filtros reales.
 *
 * Tipo: JUnit5 / @WebMvcTest
 */
@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-REPORT-003: Listar denuncias por municipalidad / filtros")
class CP_REPORT_003_ListarPorMunicipalidadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private static UsernamePasswordAuthenticationToken mockAuth(String userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    private ReportSummaryResponse buildSummary(String id, String category, String status) {
        return ReportSummaryResponse.builder()
                .reportId(id)
                .description("Denuncia de prueba número " + id)
                .category(category)
                .priority("MEDIUM")
                .status(status)
                .latitude(-33.4372)
                .longitude(-70.6506)
                .address("Calle Falsa 123, Santiago")
                .createdAt(Instant.now().toString())
                .updatedAt(Instant.now().toString())
                .build();
    }

    @Test
    @DisplayName("GET /api/reports sin filtros → 200 OK + lista completa")
    void listarSinFiltros_retorna200ConLista() throws Exception {
        ReportListResponse response = ReportListResponse.builder()
                .reports(List.of(
                        buildSummary("r001", "BACHE", "PENDING"),
                        buildSummary("r002", "ILUMINACION", "IN_PROGRESS"),
                        buildSummary("r003", "BASURA", "RESOLVED")
                ))
                .total(3)
                .page(0)
                .build();

        when(reportService.getReports(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth("admin-uuid"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.reports").isArray())
                .andExpect(jsonPath("$.reports.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/reports?category=BACHE → 200 OK + lista filtrada por categoría")
    void listarPorCategoria_retorna200ListaFiltrada() throws Exception {
        ReportListResponse response = ReportListResponse.builder()
                .reports(List.of(
                        buildSummary("r001", "BACHE", "PENDING"),
                        buildSummary("r004", "BACHE", "IN_PROGRESS")
                ))
                .total(2)
                .page(0)
                .build();

        when(reportService.getReports(isNull(), eq("BACHE"), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth("admin-uuid")))
                        .param("category", "BACHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.reports[0].category").value("BACHE"))
                .andExpect(jsonPath("$.reports[1].category").value("BACHE"));
    }

    @Test
    @DisplayName("GET /api/reports?status=PENDING → 200 OK + solo denuncias pendientes")
    void listarPorEstadoPending_retorna200ListaFiltrada() throws Exception {
        ReportListResponse response = ReportListResponse.builder()
                .reports(List.of(
                        buildSummary("r005", "AGUA", "PENDING"),
                        buildSummary("r006", "SEÑALETICA", "PENDING")
                ))
                .total(2)
                .page(0)
                .build();

        when(reportService.getReports(eq("PENDING"), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth("officer-uuid")))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports[0].status").value("PENDING"))
                .andExpect(jsonPath("$.reports[1].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/reports?category=BACHE&status=RESOLVED → 200 OK + múltiples filtros")
    void listarConMultiplesFiltros_retorna200() throws Exception {
        ReportListResponse response = ReportListResponse.builder()
                .reports(List.of(buildSummary("r007", "BACHE", "RESOLVED")))
                .total(1)
                .page(0)
                .build();

        when(reportService.getReports(eq("RESOLVED"), eq("BACHE"), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth("admin-uuid")))
                        .param("category", "BACHE")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.reports[0].category").value("BACHE"))
                .andExpect(jsonPath("$.reports[0].status").value("RESOLVED"));
    }

    @Test
    @DisplayName("GET /api/reports sin resultados → 200 OK + lista vacía")
    void listarSinResultados_retorna200ListaVacia() throws Exception {
        ReportListResponse response = ReportListResponse.builder()
                .reports(List.of())
                .total(0)
                .page(0)
                .build();

        when(reportService.getReports(any(), any(), any(), any(),
                any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth("admin-uuid")))
                        .param("category", "ARBOL_CAIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.reports").isEmpty());
    }
}
