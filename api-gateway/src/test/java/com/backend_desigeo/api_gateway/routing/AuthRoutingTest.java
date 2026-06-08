package com.backend_desigeo.api_gateway.routing;

import com.backend_desigeo.api_gateway.filter.JwtAuthenticationFilter;
import com.backend_desigeo.api_gateway.security.JwtValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.*;

/**
 * CP-GW-003 - Enrutar correctamente a AuthService
 *
 * Verifica que POST /api/auth/login sea tratado como ruta pública
 * y que el filtro JWT no bloquee la solicitud (sin necesidad de token).
 *
 * Nota: la ruta real a http://localhost:8081 se mockeará para no depender
 * del servicio de auth en los tests. El test valida el comportamiento del
 * gateway (filtro + routing) y no el downstream.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-integration-tests-256bits!!",
        // Apunta auth-service a un mock WireMock o simplemente validamos
        // que el gateway no bloquea la ruta (respuesta != 401)
        "spring.cloud.gateway.routes[0].id=auth-service",
        "spring.cloud.gateway.routes[0].uri=http://localhost:9999",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**"
})
class AuthRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    // ─────────────────────────────────────────────────────────────────────────
    // CP-GW-003: POST /api/auth/login es ruta pública → el filtro NO retorna 401
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("CP-GW-003 - POST /api/auth/login es ruta pública: el filtro no devuelve 401")
    void cp_gw_003_authLoginRoute_shouldBePublicAndNotReturn401() {
        // Arrange: body de login estándar
        String loginBody = """
                {
                  "email": "test@example.com",
                  "password": "password123"
                }
                """;

        // Act & Assert
        // El gateway reenvía la petición al downstream (http://localhost:9999 en test).
        // Si el downstream no existe devuelve 502/503, pero NUNCA 401.
        // Lo importante es que el filtro JWT no bloquea la ruta pública.
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginBody)
                .exchange()
                .expectStatus()
                .value(status ->
                        org.assertj.core.api.Assertions.assertThat(status)
                                .as("La ruta /api/auth/login no debe ser bloqueada por el filtro JWT (no debe ser 401)")
                                .isNotEqualTo(401)
                );
    }

    @Test
    @DisplayName("CP-GW-003 (variante) - POST /api/auth/login sin token: no devuelve 401 Unauthorized")
    void cp_gw_003_authLoginWithoutToken_shouldNotBeBlockedByJwtFilter() {
        // Arrange
        String loginBody = """
                {
                  "email": "admin@desigeo.cl",
                  "password": "Admin1234!"
                }
                """;

        // Act & Assert
        // Verificación explícita de que no es 401 (el filtro JWT deja pasar rutas /api/auth/**)
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginBody)
                .exchange()
                .expectStatus().value(status ->
                        org.assertj.core.api.Assertions.assertThat(status)
                                .as("Sin token, /api/auth/login debe ser accesible (ruta pública)")
                                .isNotEqualTo(401)
                );
    }
}
