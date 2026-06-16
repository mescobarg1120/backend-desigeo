package com.backend_desigeo.desigeo_notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private String notificationId;
    private String userId;
    private String type;
    private String reportId;
    private String title;
    private String message;
    private boolean read;
    private String createdAt;
}
