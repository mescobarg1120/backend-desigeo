package com.backend_desigeo.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r.path("/api/auth/**")
                .uri("http://localhost:8081"))
            .route("auth-user-service", r -> r.path("/api/users/**")
                .uri("http://localhost:8087"))
            .route("report-service", r -> r.path("/api/reports/**")
                .uri("http://localhost:8082"))
            .route("support-service", r -> r.path("/api/supports/**")
                .uri("http://localhost:8083"))
            .route("ai-service", r -> r.path("/api/ai/**")
                .uri("http://localhost:8084"))
            .route("notification-service", r -> r.path("/api/notifications/**")
                .uri("http://localhost:8085"))
            .route("analytics-service", r -> r.path("/api/analytics/**")
                .uri("http://localhost:8086"))
            .build();
    }
}
