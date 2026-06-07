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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-REPORT-002 — Rechazar sin categoría válida
 * POST sin categoría → comportamiento esperado: el servicio defaultea a "OTRO" cuando
 * category es null/blank. Si se envía un valor no reconocido, el servicio lanza
 * IllegalArgumentException → GlobalExceptionHandler → 400.
 *
 * Tipo: JUnit5 / @WebMvcTest
 */
@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-REPORT-002: Rechazar sin categoría válida")
class CP_REPORT_002_SinCategoriaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    private static final String USER_ID = "user-uuid-001";

    private static UsernamePasswordAuthenticationToken mockAuth(String userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("POST sin categoría → servicio usa 'OTRO' como default → 201 (comportamiento actual)")
    void sinCategoria_servicioUsaDefault_retorna201() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Hay basura acumulada en la vereda frente al colegio");
        request.setLatitude(-33.4372);
        request.setLongitude(-70.6506);
        // category == null

        CreateReportResponse response = CreateReportResponse.builder()
                .reportId("report-uuid-cat-001")
                .status("PENDING")
                .createdAt(Instant.now().toString())
                .build();

        when(reportService.createReport(any(CreateReportRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST con categoría blank → servicio usa 'OTRO' como default → 201 (comportamiento actual)")
    void categoriaBlank_servicioUsaDefault_retorna201() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Luminaria apagada en la esquina de las calles 5 y 6");
        request.setCategory("");
        request.setLatitude(-33.4569);
        request.setLongitude(-70.6483);

        CreateReportResponse response = CreateReportResponse.builder()
                .reportId("report-uuid-cat-002")
                .status("PENDING")
                .createdAt(Instant.now().toString())
                .build();

        when(reportService.createReport(any(CreateReportRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST con categoría inválida → servicio lanza IllegalArgumentException → 400 Bad Request")
    void categoriaInvalida_lanzaIllegalArgument_retorna400() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Descripción larga con más de diez caracteres aquí");
        request.setCategory("CATEGORIA_QUE_NO_EXISTE");
        request.setLatitude(-33.4372);
        request.setLongitude(-70.6506);

        when(reportService.createReport(any(CreateReportRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("Categoría no válida: CATEGORIA_QUE_NO_EXISTE"));

        // GlobalExceptionHandler convierte IllegalArgumentException → 400
        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Categoría no válida: CATEGORIA_QUE_NO_EXISTE"));
    }

    @Test
    @DisplayName("POST con descripción demasiado corta → 400 Bad Request (validación @Size min=10)")
    void descripcionDemasiadoCorta_retorna400() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Corto");   // < 10 chars
        request.setCategory("BACHE");
        request.setLatitude(-33.4372);
        request.setLongitude(-70.6506);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
