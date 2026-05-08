package com.backend_desigeo.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public String gatewayJwtSecret(@Value("${jwt.secret}") String jwtSecret) {
        return jwtSecret;
    }
}
