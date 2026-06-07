package com.backend_desigeo.desigeo_report_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddCommentRequest {

    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 500, message = "Máximo 500 caracteres")
    private String content;

    private String userName;
}
