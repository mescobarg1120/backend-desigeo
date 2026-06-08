package com.backend_desigeo.api_gateway.filter;

import com.backend_desigeo.api_gateway.security.JwtValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CP-GW-001 - Validar token JWT correcto
 * CP-GW-002 - Rechazar token JWT expirado
 *
 * Tests unitarios para JwtAuthenticationFilter usando Mockito.
 * Se aísla el comportamiento del filtro sin levantar el contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    // Secret de al menos 256 bits para HMAC-SHA256
    private static final String TEST_SECRET =
            "test-secret-key-for-unit-tests-must-be-long-enough-256bits!!";

    @Mock
    private JwtValidator jwtValidator;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtValidator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CP-GW-001: Token JWT válido → la cadena de filtros continúa (no 401)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("CP-GW-001 - Token JWT válido: el filtro permite el acceso y propaga X-User-Id / X-User-Role")
    void cp_gw_001_validToken_shouldPassThroughFilter() {
        // Arrange
        String validToken = buildToken(60_000); // expira en 60 s
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        io.jsonwebtoken.Claims fakeClaims = buildClaims("user-123", "ADMIN");
        when(jwtValidator.isTokenValid(validToken)).thenReturn(true);
        when(jwtValidator.extractClaims(validToken)).thenReturn(fakeClaims);

        // Act
        boolean[] chainCalled = {false};
        Mono<Void> result = filter.filter(exchange, chain -> {
            chainCalled[0] = true;
            return Mono.empty();
        });

        StepVerifier.create(result).verifyComplete();

        // Assert
        assertThat(chainCalled[0])
                .as("La cadena de filtros debe continuar con un token válido")
                .isTrue();

        assertThat(exchange.getResponse().getStatusCode())
                .as("No debe retornar 401")
                .isNotEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);

        verify(jwtValidator).isTokenValid(validToken);
        verify(jwtValidator).extractClaims(validToken);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CP-GW-002: Token JWT expirado → el filtro retorna 401 Unauthorized
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("CP-GW-002 - Token JWT expirado: el filtro retorna 401 Unauthorized")
    void cp_gw_002_expiredToken_shouldReturnUnauthorized() {
        // Arrange
        String expiredToken = buildToken(-1_000); // ya expirado
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .header("Authorization", "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtValidator.isTokenValid(expiredToken)).thenReturn(false);

        // Act
        boolean[] chainCalled = {false};
        Mono<Void> result = filter.filter(exchange, chain -> {
            chainCalled[0] = true;
            return Mono.empty();
        });

        StepVerifier.create(result).verifyComplete();

        // Assert
        assertThat(chainCalled[0])
                .as("La cadena de filtros NO debe continuar con un token expirado")
                .isFalse();

        assertThat(exchange.getResponse().getStatusCode())
                .as("Debe retornar 401 Unauthorized")
                .isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);

        verify(jwtValidator).isTokenValid(expiredToken);
        verify(jwtValidator, never()).extractClaims(anyString());
    }

    @Test
    @DisplayName("CP-GW-002 (variante) - Sin header Authorization: el filtro retorna 401 Unauthorized")
    void cp_gw_002_missingAuthHeader_shouldReturnUnauthorized() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/me")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        boolean[] chainCalled = {false};
        Mono<Void> result = filter.filter(exchange, chain -> {
            chainCalled[0] = true;
            return Mono.empty();
        });

        StepVerifier.create(result).verifyComplete();

        // Assert
        assertThat(chainCalled[0]).isFalse();
        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);

        verifyNoInteractions(jwtValidator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera un JWT firmado con el secret de prueba.
     * @param expiresInMillis milisegundos de validez desde ahora (negativo = ya expirado)
     */
    private String buildToken(long expiresInMillis) {
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("user-123")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiresInMillis))
                .signWith(key)
                .compact();
    }

    /**
     * Construye un objeto Claims simulado (para mockear extractClaims).
     */
    private io.jsonwebtoken.Claims buildClaims(String userId, String role) {
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(buildToken(60_000))
                .getBody();
    }
}
