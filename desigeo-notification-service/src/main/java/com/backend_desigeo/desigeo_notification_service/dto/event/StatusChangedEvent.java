package com.backend_desigeo.desigeo_notification_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangedEvent {

    private String reportId;
    private String citizenUserId;
    private String previousStatus;
    private String newStatus;
    private String category;
    private String address;
}
