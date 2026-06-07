package com.backend_desigeo.desigeo_report_service.service;

import com.backend_desigeo.desigeo_report_service.config.TestSecurityConfig;
import com.backend_desigeo.desigeo_report_service.controller.ReportController;
import com.backend_desigeo.desigeo_report_service.dto.request.CreateReportRequest;
import com.backend_desigeo.desigeo_report_service.dto.response.CreateReportResponse;
import com.backend_desigeo.desigeo_report_service.util.GeohashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CP-REPORT-005 — Validar geolocalización
 * POST con coordenadas dentro del perímetro → 201
 * POST con coordenadas fuera de rango → 400
 *
 * Incluye dos capas de tests:
 *   A) Tests de controller (@WebMvcTest): validaciones @DecimalMin/@DecimalMax del DTO.
 *   B) Tests unitarios de GeohashUtil: sin contexto Spring (pure JUnit5).
 *
 * Tipo: JUnit5
 */
@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CP-REPORT-005: Validar geolocalización")
class CP_REPORT_005_GeolocationTest {

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

    // ──────────────────────────────────────────────────────────────────────────
    // A) Tests de controller — validación de rangos @DecimalMin/@DecimalMax
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST con coordenadas válidas de Santiago → 201 Created")
    void coordenadasDentroDeChile_retorna201() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Bache peligroso frente al paradero de buses número 5");
        request.setCategory("BACHE");
        request.setLatitude(-33.4372);
        request.setLongitude(-70.6506);
        request.setAddress("Av. Providencia 1234, Santiago");

        CreateReportResponse response = CreateReportResponse.builder()
                .reportId("report-geo-001")
                .status("PENDING")
                .createdAt(Instant.now().toString())
                .build();

        when(reportService.createReport(any(CreateReportRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value("report-geo-001"));
    }

    @Test
    @DisplayName("POST con latitud > 90 → 400 Bad Request (@DecimalMax)")
    void latitudFueraDeRango_mayor90_retorna400() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Descripción válida con más de diez caracteres");
        request.setLatitude(91.0);       // > 90
        request.setLongitude(-70.6506);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST con latitud < -90 → 400 Bad Request (@DecimalMin)")
    void latitudFueraDeRango_menorMenos90_retorna400() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Descripción válida con más de diez caracteres");
        request.setLatitude(-91.0);
        request.setLongitude(-70.6506);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST con longitud > 180 → 400 Bad Request (@DecimalMax)")
    void longitudFueraDeRango_mayor180_retorna400() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Descripción válida con más de diez caracteres");
        request.setLatitude(-33.4372);
        request.setLongitude(181.0);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST con longitud < -180 → 400 Bad Request (@DecimalMin)")
    void longitudFueraDeRango_menorMenos180_retorna400() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setDescription("Descripción válida con más de diez caracteres");
        request.setLatitude(-33.4372);
        request.setLongitude(-181.0);

        mockMvc.perform(post("/api/reports")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(mockAuth(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // B) Tests unitarios de GeohashUtil — JUnit5 puro, sin contexto Spring
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GeohashUtil: coordenadas de Santiago generan geohash no vacío de 7 caracteres")
    void geohashUtil_santiago_generaHashValido() {
        String hash = GeohashUtil.encode(-33.4372, -70.6506);
        assertAll(
                () -> assertNotNull(hash),
                () -> assertFalse(hash.isBlank()),
                () -> assertEquals(7, hash.length()),
                () -> assertThat(hash).matches("[0-9bcdefghjkmnpqrstuvwxyz]+")
        );
    }

    @Test
    @DisplayName("GeohashUtil: mismas coordenadas producen el mismo geohash (determinismo)")
    void geohashUtil_mismasCoordenadas_producenMismoHash() {
        String hash1 = GeohashUtil.encode(-33.4372, -70.6506);
        String hash2 = GeohashUtil.encode(-33.4372, -70.6506);
        assertEquals(hash1, hash2);
    }

    @ParameterizedTest(name = "lat={0}, lon={1} → geohash de {2} chars")
    @CsvSource({
            "-33.4372, -70.6506, 7",
            "-33.5570, -70.6370, 7",
            "-33.3800, -70.5800, 7",
            "-23.6500, -70.3960, 7",
            "-51.7200, -72.5000, 7"
    })
    @DisplayName("GeohashUtil: ciudades chilenas generan geohash de longitud correcta")
    void geohashUtil_ciudadesChilenas_generanHashDeLongitudCorrecta(
            double lat, double lon, int expectedLength) {
        assertEquals(expectedLength, GeohashUtil.encode(lat, lon).length());
    }

    @Test
    @DisplayName("GeohashUtil: coordenadas cercanas comparten prefijo de 5 caracteres")
    void geohashUtil_coordenadasCercanas_tienenPrefijoComun() {
        String hash1 = GeohashUtil.encode(-33.4372, -70.6506);
        String hash2 = GeohashUtil.encode(-33.4375, -70.6510);
        assertEquals(hash1.substring(0, 5), hash2.substring(0, 5));
    }

    @Test
    @DisplayName("GeohashUtil: coordenadas lejanas producen geohashes distintos")
    void geohashUtil_coordenadasLejanas_producenHashesDistintos() {
        assertNotEquals(
                GeohashUtil.encode(-33.4372, -70.6506),
                GeohashUtil.encode(-23.6500, -70.3960)
        );
    }
}
