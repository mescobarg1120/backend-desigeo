package com.backend_desigeo.desigeo_report_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportSummaryResponse {
    private String reportId;
    private String description;
    private String category;
    private String priority;
    private String status;
    private Double latitude;
    private Double longitude;
    private String address;
    private String createdAt;
    private String updatedAt;
}
