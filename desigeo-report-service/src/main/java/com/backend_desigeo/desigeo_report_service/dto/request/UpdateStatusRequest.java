package com.backend_desigeo.desigeo_report_service.dto.request;

import com.backend_desigeo.desigeo_report_service.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private ReportStatus status;

    private String comment;
}
