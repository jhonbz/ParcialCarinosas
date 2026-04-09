package com.carinosas.people_service.repository;

import com.carinosas.people_service.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    // Magia de Spring para buscar a todos los involucrados en un caso
    List<Person> findByCaseId(Long caseId);
}