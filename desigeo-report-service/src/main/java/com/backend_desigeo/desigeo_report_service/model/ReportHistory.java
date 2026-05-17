package com.backend_desigeo.desigeo_report_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistory {
    private String historyId;
    private String reportId;
    private String timestamp;
    private String previousStatus;
    private String newStatus;
    private String comment;
    private String changedBy;
    private String changedByRole;
    private String reopenReason;
}
