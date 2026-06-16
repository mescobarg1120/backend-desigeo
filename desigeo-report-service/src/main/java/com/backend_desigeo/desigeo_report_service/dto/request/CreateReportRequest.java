package com.backend_desigeo.desigeo_report_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateReportRequest {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000)
    private String description;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;

    private String address;

    private String category;

    private String comunaNombre;

    @Size(max = 5, message = "Máximo 5 imágenes permitidas")
    private List<String> images;
}
