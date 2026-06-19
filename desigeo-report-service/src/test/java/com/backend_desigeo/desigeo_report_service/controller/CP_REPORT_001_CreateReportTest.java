package com.backend_desigeo.desigeo_report_service.controller;

import com.backend_desigeo.desigeo_report_service.config.TestSecurityConfig;
import com.backend_desigeo.desigeo_report_service.dto.request.CreateReportRequest;
import com.backend_desigeo.desigeo_report_service.dto.response.CreateReportResponse;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-REPORT-001 — Crear denuncia exitosamente
 * POST /api/reports + descripción + categoría + ubicación → 201 Created
 *
 * Tipo: Testcontainers (plan original). Implementado con @WebMvcTest + MockBean
 * dado que el backend usa Firestore (no PostgreSQL para reportes), lo que hace
 * que Testcontainers-PostgreSQL no aplique al repositorio de denuncias.
 *
 * Nota sobre notificaciones: el historial de creación se guarda via saveHistory()
 * dentro del servicio. No existe servicio de notificaciones push en este microservicio.
 */
@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-REPORT-001: Crear denuncia exitosamente")
class CP_REPORT_001_CreateReportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    // Principal como String (el controller hace (String) auth.getPrincipal())
    private static final String USER_ID = "user-uuid-001";

    private static UsernamePasswordAuthenticationToken mockAuth(String userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("POST /api/reports con descripción, categoría y ubicación válidas → 201 Created")
    void crearDenunciaConCamposValidos_retorna201() throws Exception {
        // Arrange — body de la petición
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Hay un bache muy grande en la calle principal");
        request.setCategory("BACHE");
        request.setLatitude(-33.4372);
        request.setLongitude(-70.6506);
        request.setAddress("Av. Providencia 1234, Santiago");

        // Arrange — respuesta esperada del servicio
        CreateReportResponse response = CreateReportResponse.builder()
                .reportId("report-uuid-001")
                .status("PENDING")
                .createdAt(Instant.now().toString())
                .build();

        when(reportService.createReport(any(CreateReportRequest.class), anyString()))
                .thenReturn(response);

        // Act & Assert — principal como String para que (String) auth.getPrincipal() funcione
        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value("report-uuid-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        verify(reportService).createReport(any(CreateReportRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/reports con categoría ILUMINACION → 201 Created con status PENDING")
    void crearDenunciaIluminacion_retorna201YStatusPending() throws Exception {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("El alumbrado público lleva 3 semanas apagado en esta cuadra");
        request.setCategory("ILUMINACION");
        request.setLatitude(-33.4569);
        request.setLongitude(-70.6483);

        CreateReportResponse response = CreateReportResponse.builder()
                .reportId("report-uuid-002")
                .status("PENDING")
                .createdAt(Instant.now().toString())
                .build();

        when(reportService.createReport(any(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/reports sin descripción → 400 Bad Request (validación @NotBlank)")
    void crearDenunciaSinDescripcion_retorna400() throws Exception {
        // Arrange — descripción ausente → @NotBlank falla
        CreateReportRequest request = new CreateReportRequest();
        request.setCategory("BACHE");
        request.setLatitude(-33.4372);
        request.setLongitude(-70.6506);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reports sin coordenadas → 400 Bad Request (validación @NotNull)")
    void crearDenunciaSinCoordenadas_retorna400() throws Exception {
        // Arrange — sin latitud ni longitud → @NotNull falla
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Descripción de prueba con al menos diez caracteres");
        request.setCategory("BASURA");

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
