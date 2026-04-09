package com.carinosas.workflow_service.repository;

import com.carinosas.workflow_service.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Método mágico de Spring Data para buscar las tareas de un rol específico
    List<Task> findByAssignedRole(String assignedRole);
}