package com.backend_desigeo.api_gateway.filter;

import com.backend_desigeo.api_gateway.security.JwtValidator;

import io.jsonwebtoken.Claims;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/",
        "/api/auth",
        "/actuator",
        "/actuator/",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-ui/",
        "/api/users"
    );

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring("Bearer ".length());
        if (!jwtValidator.isTokenValid(token)) {
            return unauthorized(exchange);
        }
        Claims claims = jwtValidator.extractClaims(token);
        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(r -> r
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Role", claims.get("role", String.class))
            )
            .build();

    return chain.filter(mutatedExchange);
        
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        byte[] body = "{\"error\":\"Unauthorized\"}".getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
