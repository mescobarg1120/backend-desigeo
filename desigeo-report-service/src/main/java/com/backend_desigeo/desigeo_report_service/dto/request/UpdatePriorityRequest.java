package com.backend_desigeo.desigeo_report_service.dto.request;

import com.backend_desigeo.desigeo_report_service.enums.ReportPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePriorityRequest {

    @NotNull(message = "La prioridad es obligatoria")
    private ReportPriority priority;
}
