package com.carinosas.evidence_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "evidences")
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aquí guardamos el ID del caso al que pertenece esta evidencia
    private Long caseId;

    private String name; // Ej: "Cuerdas doradas" o "Monedas de oro"

    private String description;

    private String attachedFileUrl; // El requerimiento pide "incluye adjuntos"

    // Un texto o formato JSON simple para guardar por quién ha pasado la evidencia
    @Column(length = 1000)
    private String custodyChainHistory;

    private LocalDateTime createdAt;

    // Esto hace que la fecha se llene automáticamente cuando se crea la evidencia
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}