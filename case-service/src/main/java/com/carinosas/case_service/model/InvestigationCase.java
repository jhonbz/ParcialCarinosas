package com.carinosas.case_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data // Esta maravilla de Lombok nos crea los Getters y Setters automáticamente
@Entity // Le dice a Spring que esto es una tabla de la base de datos
@Table(name = "investigation_cases")
public class InvestigationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String status; // Ej: ABIERTO, CERRADO, EN_PROGRESO

    private String priority; // Ej: ALTA, MEDIA, BAJA

    private String assignedTo; // ID o username del detective a cargo
}
