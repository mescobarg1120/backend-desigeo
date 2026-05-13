package com.backend_desigeo.desigeo_report_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReportListResponse {
    private List<ReportSummaryResponse> reports;
    private int total;
    private int page;
}
