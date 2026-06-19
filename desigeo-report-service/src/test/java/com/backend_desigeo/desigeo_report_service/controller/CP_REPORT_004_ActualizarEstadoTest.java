package com.backend_desigeo.desigeo_report_service.controller;

import com.backend_desigeo.desigeo_report_service.config.TestSecurityConfig;
import com.backend_desigeo.desigeo_report_service.dto.request.UpdateStatusRequest;
import com.backend_desigeo.desigeo_report_service.dto.response.ReportDetailResponse;
import com.backend_desigeo.desigeo_report_service.enums.ReportStatus;
import com.backend_desigeo.desigeo_report_service.exception.ReportNotFoundException;
import com.backend_desigeo.desigeo_report_service.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-REPORT-004 — Actualizar estado de denuncia
 * PATCH /api/reports/{id}/status + status='IN_PROGRESS' → 200 + historial actualizado
 *
 * Nota: el enum real usa valores en inglés. "EN_REVISION" equivale a IN_PROGRESS.
 * Nota sobre notificación: el historial se guarda via saveHistory() dentro del servicio.
 * No hay servicio de notificaciones push en este microservicio.
 *
 * Tipo: Testcontainers (plan original). Implementado con @WebMvcTest + MockBean
 * por las mismas razones que CP-REPORT-001 (backend Firestore).
 */
@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-REPORT-004: Actualizar estado de denuncia")
class CP_REPORT_004_ActualizarEstadoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    private static UsernamePasswordAuthenticationToken mockAuth(String userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private ReportDetailResponse buildDetailResponse(String reportId, String status) {
        return ReportDetailResponse.builder()
                .reportId(reportId)
                .userId("user-uuid-001")
                .description("Bache en la calle principal frente al mercado")
                .category("BACHE")
                .priority("MEDIUM")
                .status(status)
                .latitude(-33.4372)
                .longitude(-70.6506)
                .address("Av. Providencia 1234, Santiago")
                .images(List.of())
                .createdAt(Instant.now().toString())
                .updatedAt(Instant.now().toString())
                .history(List.of(
                        ReportDetailResponse.HistoryItem.builder()
                                .previousStatus("PENDING")
                                .newStatus(status)
                                .comment("Estado actualizado por oficial")
                                .changedBy("officer-uuid-001")
                                .changedByRole("MUNICIPAL_OFFICER")
                                .timestamp(Instant.now().toString())
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/status con status=IN_PROGRESS → 200 OK + historial actualizado")
    void actualizarEstadoAInProgress_retorna200() throws Exception {
        String reportId = "report-uuid-004";
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(ReportStatus.IN_PROGRESS);
        request.setComment("Equipo de mantenimiento asignado");

        when(reportService.updateStatus(eq(reportId), any(UpdateStatusRequest.class),
                anyString(), anyString()))
                .thenReturn(buildDetailResponse(reportId, "IN_PROGRESS"));

        mockMvc.perform(patch("/api/reports/{id}/status", reportId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                mockAuth("officer-uuid-001", "MUNICIPAL_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history[0].newStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.history[0].changedByRole").value("MUNICIPAL_OFFICER"));

        verify(reportService).updateStatus(eq(reportId), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/status con status=RESOLVED → 200 OK")
    void actualizarEstadoAResolved_retorna200() throws Exception {
        String reportId = "report-uuid-005";
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setComment("Bache reparado exitosamente");

        when(reportService.updateStatus(eq(reportId), any(), anyString(), anyString()))
                .thenReturn(buildDetailResponse(reportId, "RESOLVED"));

        mockMvc.perform(patch("/api/reports/{id}/status", reportId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                mockAuth("officer-uuid-001", "MUNICIPAL_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/status para reporte inexistente → 404 Not Found")
    void actualizarEstadoReporteInexistente_retorna404() throws Exception {
        String reportId = "report-uuid-no-existe";
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(ReportStatus.IN_PROGRESS);

        when(reportService.updateStatus(eq(reportId), any(), anyString(), anyString()))
                .thenThrow(new ReportNotFoundException(reportId));

        // GlobalExceptionHandler convierte ReportNotFoundException → 404
        mockMvc.perform(patch("/api/reports/{id}/status", reportId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                mockAuth("officer-uuid-001", "MUNICIPAL_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reporte no encontrado: " + reportId));
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/status sin campo status → 400 Bad Request (@NotNull)")
    void actualizarEstadoSinStatus_retorna400() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        // status == null → @NotNull falla

        mockMvc.perform(patch("/api/reports/{id}/status", "report-uuid-006")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                mockAuth("officer-uuid-001", "MUNICIPAL_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH con cada estado válido del enum → 200 OK (todos los estados aceptados)")
    void actualizarConCadaEstadoValido_retorna200() throws Exception {
        for (ReportStatus status : ReportStatus.values()) {
            String reportId = "report-uuid-status-" + status.name();
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(status);

            when(reportService.updateStatus(eq(reportId), any(), anyString(), anyString()))
                    .thenReturn(buildDetailResponse(reportId, status.name()));

            mockMvc.perform(patch("/api/reports/{id}/status", reportId)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    mockAuth("officer-uuid-001", "MUNICIPAL_OFFICER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status.name()));
        }
    }
}
