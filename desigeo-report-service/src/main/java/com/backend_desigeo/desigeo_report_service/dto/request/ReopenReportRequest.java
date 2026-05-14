package com.backend_desigeo.desigeo_report_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReopenReportRequest {

    @NotBlank(message = "La razón dereapertura es obligatoria")
    private String reason;
}
