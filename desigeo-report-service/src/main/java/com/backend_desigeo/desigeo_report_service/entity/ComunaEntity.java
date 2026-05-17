package com.backend_desigeo.desigeo_report_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comunas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComunaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comunaid")
    private Integer comunaId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "region")
    private String region;

    @Column(name = "codigoine")
    private String codigoIne;

    @Column(name = "isactive")
    private Boolean isActive;
}
