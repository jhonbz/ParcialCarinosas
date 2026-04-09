package com.carinosas.workflow_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // Aquí guardaremos a qué rol pertenece (ej: "ADMIN", "DETECTIVE")
    private String assignedRole;

    // Estado de la tarea (ej: "PENDIENTE", "COMPLETADA")
    private String status;

    private Long caseId;
}