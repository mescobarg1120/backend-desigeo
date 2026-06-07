package com.backend_desigeo.desigeo_report_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Configuración de seguridad para tests.
 *
 * Proporciona:
 *  1. SecurityFilterChain que permite todo (sin JWT) — reemplaza la cadena de producción.
 *  2. Un filtro NoOp que sustituye al JwtAuthenticationFilter real, pasando
 *     siempre la request al controller sin intentar validar tokens.
 *
 * Uso: @WebMvcTest(Xxx.class) + @Import(TestSecurityConfig.class)
 * Los tests deben proporcionar el Authentication via
 * SecurityMockMvcRequestPostProcessors.authentication(...) para que el
 * controller pueda leer auth.getPrincipal() como String.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Filtro que reemplaza JwtAuthenticationFilter en tests.
     * No hace nada con el token — simplemente pasa al siguiente filtro.
     */
    @Bean
    @Primary
    public com.backend_desigeo.desigeo_report_service.security.JwtAuthenticationFilter
    jwtAuthenticationFilterNoOp(
            com.backend_desigeo.desigeo_report_service.security.JwtUtils jwtUtils) {
        return new com.backend_desigeo.desigeo_report_service.security.JwtAuthenticationFilter(jwtUtils) {
            @Override
            protected void doFilterInternal(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain)
                    throws ServletException, IOException {
                chain.doFilter(req, res);   // pass-through: sin validar JWT
            }
        };
    }

    /**
     * JwtUtils con secret dummy para que el bean se cree sin error.
     * Los tests no usan tokens reales, así que el valor no importa.
     */
    @Bean
    @Primary
    public com.backend_desigeo.desigeo_report_service.security.JwtUtils jwtUtilsNoOp() {
        return new com.backend_desigeo.desigeo_report_service.security.JwtUtils(
                "test-secret-minimum-256-bits-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
    }

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
