package com.backend_desigeo.desigeo_notification_service.controller;

import com.backend_desigeo.desigeo_notification_service.dto.event.ReportCreatedEvent;
import com.backend_desigeo.desigeo_notification_service.dto.event.ReopenRequestedEvent;
import com.backend_desigeo.desigeo_notification_service.dto.event.StatusChangedEvent;
import com.backend_desigeo.desigeo_notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/report-created")
    public ResponseEntity<Void> reportCreated(@RequestBody ReportCreatedEvent event) {
        log.info("Received report-created event for reportId={}", event.getReportId());
        notificationService.handleReportCreated(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/status-changed")
    public ResponseEntity<Void> statusChanged(@RequestBody StatusChangedEvent event) {
        log.info("Received status-changed event for reportId={}", event.getReportId());
        notificationService.handleStatusChanged(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reopen-requested")
    public ResponseEntity<Void> reopenRequested(@RequestBody ReopenRequestedEvent event) {
        log.info("Received reopen-requested event for reportId={}", event.getReportId());
        notificationService.handleReopenRequested(event);
        return ResponseEntity.ok().build();
    }
}
