
package com.backend_desigeo.gestion_de_usuarios.dto;
 
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
 
@Data
@Builder
public class ComunaDto {
 
    private Integer comunaId;
    private String nombre;
    private String region;
    private String codigoIne;
    private Boolean isActive;
    private Instant createdAt;
    private Long agentCount;
    private String adminEmail;
    private String adminName;
}