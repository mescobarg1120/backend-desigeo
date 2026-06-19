package com.backend_desigeo.desigeo_analytics_service.config;

import com.backend_desigeo.desigeo_analytics_service.security.GatewayAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

/**
 * Configuración de seguridad para tests de @WebMvcTest.
 *
 * Proporciona:
 *  1. GatewayAuthFilter pass-through — simplemente delega sin leer headers de auth.
 *     Esto evita que el filtro mockeado corte la cadena antes de llegar al controller.
 *  2. SecurityFilterChain que permite todas las peticiones sin autenticación.
 *     La lógica de autorización por rol se prueba a través del header X-User-Role
 *     que el controller lee directamente (sin Spring Security).
 *
 * Uso: @WebMvcTest(XxxController.class) + @Import(TestSecurityConfig.class)
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * GatewayAuthFilter que no hace nada con los headers — solo pasa al controller.
     * Sobreescribe el @Component real en el contexto de test.
     */
    @Bean
    @Primary
    public GatewayAuthFilter gatewayAuthFilterPassThrough() {
        return new GatewayAuthFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain)
                    throws ServletException, IOException {
                chain.doFilter(req, res);
            }
        };
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
