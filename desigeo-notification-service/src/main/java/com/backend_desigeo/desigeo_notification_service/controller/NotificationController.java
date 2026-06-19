package com.backend_desigeo.desigeo_notification_service.controller;

import com.backend_desigeo.desigeo_notification_service.dto.NotificationResponse;
import com.backend_desigeo.desigeo_notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication auth) {
        log.info("GET /api/notifications — auth={}, principal={}", auth, auth != null ? auth.getPrincipal() : "NULL");
        String userId = (String) auth.getPrincipal();
        List<NotificationResponse> result = notificationService.getNotificationsForUser(userId);
        log.info("GET /api/notifications — returning {} notifications for userId={}", result.size(), userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
