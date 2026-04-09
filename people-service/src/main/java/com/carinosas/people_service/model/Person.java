package com.carinosas.people_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "people")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long caseId; // El ID del caso en el Case Service

    private String name;

    private String dni; // Número de identificación

    private Integer age; // Edad

    private String address; // Dirección

    private String alias; // Alias o apodo

    @Enumerated(EnumType.STRING)
    private PersonRole role; // Rol en el caso

    // Una coartada o declaración (vital para sospechosos o testigos)
    @Column(length = 1000)
    private String statementOrAlibi;

    private String contactInfo;
}