package com.backend_desigeo.gestion_de_usuarios.dto;
 
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
 
@Data
public class ComunaCreateDto {
 
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
 
    private String region;
 
    private String codigoIne;
}