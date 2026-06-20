package com.backend_desigeo.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.user-service.url:http://localhost:8087}")
    private String userServiceUrl;

    @Value("${services.report-service.url:http://localhost:8082}")
    private String reportServiceUrl;

    @Value("${services.support-service.url:http://localhost:8083}")
    private String supportServiceUrl;

    @Value("${services.ai-service.url:http://localhost:8084}")
    private String aiServiceUrl;

    @Value("${services.notification-service.url:http://localhost:8085}")
    private String notificationServiceUrl;

    @Value("${services.analytics-service.url:http://localhost:8086}")
    private String analyticsServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r.path("/api/auth/**")
                .uri(authServiceUrl))
            .route("auth-user-service", r -> r.path("/api/users/**")
                .uri(userServiceUrl))
            .route("comuna-service", r -> r.path("/api/comunas/**")
                .uri(userServiceUrl))
            .route("audit-service", r -> r.path("/api/audit/**")
                .uri(userServiceUrl))
            .route("report-service", r -> r.path("/api/reports/**")
                .uri(reportServiceUrl))
            .route("support-service", r -> r.path("/api/supports/**")
                .uri(supportServiceUrl))
            .route("ai-service", r -> r.path("/api/ai/**")
                .uri(aiServiceUrl))
            .route("notification-service", r -> r.path("/api/notifications", "/api/notifications/**")
                .uri(notificationServiceUrl))
            .route("analytics-service", r -> r.path("/api/analytics/**")
                .uri(analyticsServiceUrl))
            .build();
    }
}
