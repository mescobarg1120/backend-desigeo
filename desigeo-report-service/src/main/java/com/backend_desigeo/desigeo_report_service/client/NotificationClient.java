package com.backend_desigeo.desigeo_report_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClient(@Value("${notification-service.url}") String notificationServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void notifyReportCreated(String reportId, String citizenUserId, String category, String address) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("reportId", reportId);
            body.put("citizenUserId", citizenUserId);
            body.put("category", category != null ? category : "OTRO");
            body.put("address", address != null ? address : "");

            post("/internal/notifications/report-created", body);
        } catch (Exception e) {
            log.warn("Failed to notify report-created for reportId={}: {}", reportId, e.getMessage());
        }
    }

    public void notifyStatusChanged(String reportId, String citizenUserId, String previousStatus, String newStatus, String category, String address) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("reportId", reportId);
            body.put("citizenUserId", citizenUserId);
            body.put("previousStatus", previousStatus);
            body.put("newStatus", newStatus);
            body.put("category", category != null ? category : "");
            body.put("address", address != null ? address : "");

            post("/internal/notifications/status-changed", body);
        } catch (Exception e) {
            log.warn("Failed to notify status-changed for reportId={}: {}", reportId, e.getMessage());
        }
    }

    public void notifyReopenRequested(String reportId, String citizenUserId, String reason) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("reportId", reportId);
            body.put("citizenUserId", citizenUserId);
            body.put("reason", reason != null ? reason : "");

            post("/internal/notifications/reopen-requested", body);
        } catch (Exception e) {
            log.warn("Failed to notify reopen-requested for reportId={}: {}", reportId, e.getMessage());
        }
    }

    private void post(String path, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(notificationServiceUrl + path, entity, Void.class);
    }
}
