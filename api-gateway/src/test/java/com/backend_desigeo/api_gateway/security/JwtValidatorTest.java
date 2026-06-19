package com.backend_desigeo.api_gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para JwtValidator.
 * Complementan CP-GW-001 y CP-GW-002 validando la lógica de parseo de tokens.
 */
class JwtValidatorTest {

    private static final String SECRET =
            "test-secret-key-for-unit-tests-must-be-long-enough-256bits!!";

    private JwtValidator jwtValidator;
    private Key key;

    @BeforeEach
    void setUp() {
        jwtValidator = new JwtValidator(SECRET);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // CP-GW-001 - Token válido
    @Test
    @DisplayName("CP-GW-001 - isTokenValid: token válido y vigente debe retornar true")
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = Jwts.builder()
                .subject("user-42")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtValidator.isTokenValid(token)).isTrue();
    }

    // CP-GW-001 - extractClaims devuelve los claims correctos
    @Test
    @DisplayName("CP-GW-001 - extractClaims: debe extraer subject y role del token válido")
    void extractClaims_withValidToken_shouldReturnCorrectClaims() {
        String token = Jwts.builder()
                .subject("user-42")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        var claims = jwtValidator.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user-42");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    // CP-GW-002 - Token expirado
    @Test
    @DisplayName("CP-GW-002 - isTokenValid: token expirado debe retornar false")
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        String expiredToken = Jwts.builder()
                .subject("user-42")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000)) // ya expiró
                .signWith(key)
                .compact();

        assertThat(jwtValidator.isTokenValid(expiredToken)).isFalse();
    }

    // CP-GW-002 - Token con firma inválida
    @Test
    @DisplayName("CP-GW-002 (variante) - isTokenValid: token con firma incorrecta debe retornar false")
    void isTokenValid_withWrongSignature_shouldReturnFalse() {
        Key wrongKey = Keys.hmacShaKeyFor(
                "completely-different-secret-key-256bits-padding!!".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = Jwts.builder()
                .subject("hacker")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey)
                .compact();

        assertThat(jwtValidator.isTokenValid(tamperedToken)).isFalse();
    }

    // CP-GW-002 - Token malformado
    @Test
    @DisplayName("CP-GW-002 (variante) - isTokenValid: token malformado debe retornar false")
    void isTokenValid_withMalformedToken_shouldReturnFalse() {
        assertThat(jwtValidator.isTokenValid("not.a.valid.jwt.token")).isFalse();
        assertThat(jwtValidator.isTokenValid("")).isFalse();
        assertThat(jwtValidator.isTokenValid("Bearer abc123")).isFalse();
    }
}
