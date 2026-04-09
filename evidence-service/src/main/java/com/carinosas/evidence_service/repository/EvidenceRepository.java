package com.carinosas.evidence_service.repository;

import com.carinosas.evidence_service.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    // ¡Magia de Spring! Solo con nombrar el método así,
    // Spring crea automáticamente la consulta SQL: SELECT * FROM evidences WHERE case_id = ?
    List<Evidence> findByCaseId(Long caseId);
}