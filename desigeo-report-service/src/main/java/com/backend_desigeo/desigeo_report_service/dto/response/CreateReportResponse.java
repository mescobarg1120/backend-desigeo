package com.backend_desigeo.desigeo_report_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReportResponse {
    private String reportId;
    private String status;
    private String createdAt;
}
