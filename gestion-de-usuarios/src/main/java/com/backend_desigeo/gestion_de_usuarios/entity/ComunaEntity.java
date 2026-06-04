package com.backend_desigeo.gestion_de_usuarios.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
 
@Data
@Entity
@Table(name = "comunas")
public class ComunaEntity {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comunaid")
    private Integer comunaId;
 
    @Column(name = "nombre", nullable = false)
    private String nombre;
 
    @Column(name = "region")
    private String region;
 
    @Column(name = "codigoine")
    private String codigoIne;
 
    @Column(name = "isactive")
    private Boolean isActive = Boolean.TRUE;
 
    @Column(name = "createdat")
    private Instant createdAt;
}
