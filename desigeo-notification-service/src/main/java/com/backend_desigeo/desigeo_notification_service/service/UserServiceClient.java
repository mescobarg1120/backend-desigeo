package com.backend_desigeo.desigeo_notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public List<Map<String, Object>> getMunicipalOfficers() {
        try {
            var response = restTemplate.exchange(
                    userServiceUrl + "/api/users",
                    HttpMethod.GET,
                    internalAuthEntity(),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            List<Map<String, Object>> users = response.getBody();
            if (users == null) return Collections.emptyList();

            return users.stream()
                    .filter(u -> {
                        Object role = u.get("roleName");
                        return role != null && !role.toString().equals("CITIZEN");
                    })
                    .filter(u -> Boolean.TRUE.equals(u.get("active")))
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching officers from user-service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getUserById(String userId) {
        try {
            var response = restTemplate.exchange(
                    userServiceUrl + "/api/users/" + userId,
                    HttpMethod.GET,
                    internalAuthEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching user {} from user-service: {}", userId, e.getMessage());
            return null;
        }
    }

    private HttpEntity<Void> internalAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "notification-service");
        headers.set("X-User-Role", "ADMIN_MASTER");
        return new HttpEntity<>(headers);
    }
}
