package com.backend_desigeo.desigeo_report_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReportDetailResponse {
    private String reportId;
    private String userId;
    private String description;
    private String category;
    private String priority;
    private String status;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer comunaId;
    private List<String> images;
    private String createdAt;
    private String updatedAt;
    private List<HistoryItem> history;

    @Data
    @Builder
    public static class HistoryItem {
        private String previousStatus;
        private String newStatus;
        private String comment;
        private String changedBy;
        private String changedByRole;
        private String timestamp;
        private String reopenReason;
    }
}
