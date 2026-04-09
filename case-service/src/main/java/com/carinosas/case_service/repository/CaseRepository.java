package com.carinosas.case_service.repository;

import com.carinosas.case_service.model.InvestigationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<InvestigationCase, Long> {
    // ¡Vacío! Al extender JpaRepository, Spring Boot nos regala los métodos
    // save(), findAll(), findById(), deleteById() automáticamente.
}