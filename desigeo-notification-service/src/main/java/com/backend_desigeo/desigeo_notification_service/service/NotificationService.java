package com.backend_desigeo.desigeo_notification_service.service;

import com.backend_desigeo.desigeo_notification_service.dto.NotificationResponse;
import com.backend_desigeo.desigeo_notification_service.dto.event.ReportCreatedEvent;
import com.backend_desigeo.desigeo_notification_service.dto.event.ReopenRequestedEvent;
import com.backend_desigeo.desigeo_notification_service.dto.event.StatusChangedEvent;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String COLLECTION = "notifications";

    private final FirebaseApp firebaseApp;
    private final UserServiceClient userServiceClient;
    private final EmailService emailService;

    private Firestore db() {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    public void handleReportCreated(ReportCreatedEvent event) {
        List<Map<String, Object>> officers = userServiceClient.getMunicipalOfficers();
        for (Map<String, Object> officer : officers) {
            String userId = String.valueOf(officer.get("userId"));
            String email = (String) officer.get("email");

            String title = "Nuevo reporte ciudadano";
            String message = "Nuevo reporte de " + event.getCategory() + " en " + event.getAddress();

            saveNotification(userId, "REPORT_CREATED", event.getReportId(), title, message);

            if (email != null && !email.isBlank()) {
                emailService.sendNotificationEmail(
                        email,
                        "Nuevo reporte ciudadano - DESIGEO",
                        emailService.buildReportCreatedHtml(event.getCategory(), event.getAddress(), event.getReportId())
                );
            }
        }
    }

    public void handleStatusChanged(StatusChangedEvent event) {
        Map<String, Object> citizen = userServiceClient.getUserById(event.getCitizenUserId());
        if (citizen == null) return;

        String email = (String) citizen.get("email");
        String title = "Tu reporte cambió de estado";
        String location = (event.getAddress() != null && !event.getAddress().isBlank())
                ? event.getAddress()
                : (event.getCategory() != null ? event.getCategory() : "sin dirección");
        String message = "Tu reporte de " + location + " pasó de " + event.getPreviousStatus() + " a " + event.getNewStatus();

        saveNotification(event.getCitizenUserId(), "STATUS_CHANGED", event.getReportId(), title, message);

        if (email != null && !email.isBlank()) {
            emailService.sendNotificationEmail(
                    email,
                    "Estado de tu reporte actualizado - DESIGEO",
                    emailService.buildStatusChangedHtml(event.getReportId(), event.getPreviousStatus(), event.getNewStatus())
            );
        }
    }

    public void handleReopenRequested(ReopenRequestedEvent event) {
        List<Map<String, Object>> officers = userServiceClient.getMunicipalOfficers();
        for (Map<String, Object> officer : officers) {
            String userId = String.valueOf(officer.get("userId"));
            String email = (String) officer.get("email");

            String title = "Solicitud de reapertura";
            String message = "Se solicitó reabrir un reporte: " + event.getReason();

            saveNotification(userId, "REOPEN_REQUESTED", event.getReportId(), title, message);

            if (email != null && !email.isBlank()) {
                emailService.sendNotificationEmail(
                        email,
                        "Solicitud de reapertura de reporte - DESIGEO",
                        emailService.buildReopenRequestedHtml(event.getReportId(), event.getReason())
                );
            }
        }
    }

    public List<NotificationResponse> getNotificationsForUser(String userId) {
        try {
            QuerySnapshot snapshot = db().collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .get();

            return snapshot.getDocuments().stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching notifications for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public long getUnreadCount(String userId) {
        try {
            AggregateQuerySnapshot snapshot = db().collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("read", false)
                    .count()
                    .get()
                    .get();
            return snapshot.getCount();
        } catch (Exception e) {
            log.error("Error counting unread notifications for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }

    public void markAsRead(String notificationId, String userId) {
        try {
            DocumentReference ref = db().collection(COLLECTION).document(notificationId);
            DocumentSnapshot doc = ref.get().get();
            if (doc.exists() && userId.equals(doc.getString("userId"))) {
                ref.update("read", true).get();
            }
        } catch (Exception e) {
            log.error("Error marking notification {} as read: {}", notificationId, e.getMessage());
        }
    }

    public void markAllAsRead(String userId) {
        try {
            QuerySnapshot snapshot = db().collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("read", false)
                    .get()
                    .get();

            WriteBatch batch = db().batch();
            snapshot.getDocuments().forEach(doc -> batch.update(doc.getReference(), "read", true));
            batch.commit().get();
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user {}: {}", userId, e.getMessage());
        }
    }

    private void saveNotification(String userId, String type, String reportId, String title, String message) {
        try {
            String id = UUID.randomUUID().toString();
            Map<String, Object> data = new HashMap<>();
            data.put("notificationId", id);
            data.put("userId", userId);
            data.put("type", type);
            data.put("reportId", reportId);
            data.put("title", title);
            data.put("message", message);
            data.put("read", false);
            data.put("createdAt", Instant.now().toString());

            db().collection(COLLECTION).document(id).set(data).get();
        } catch (Exception e) {
            log.error("Error saving notification for user {}: {}", userId, e.getMessage());
        }
    }

    private NotificationResponse toResponse(DocumentSnapshot doc) {
        return NotificationResponse.builder()
                .notificationId(doc.getString("notificationId"))
                .userId(doc.getString("userId"))
                .type(doc.getString("type"))
                .reportId(doc.getString("reportId"))
                .title(doc.getString("title"))
                .message(doc.getString("message"))
                .read(Boolean.TRUE.equals(doc.getBoolean("read")))
                .createdAt(doc.getString("createdAt"))
                .build();
    }
}
