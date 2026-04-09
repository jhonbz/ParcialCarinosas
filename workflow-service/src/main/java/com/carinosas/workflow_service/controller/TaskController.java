package com.carinosas.workflow_service.controller;

import com.carinosas.workflow_service.model.Task;
import com.carinosas.workflow_service.repository.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final String caseServiceUrl;

    public TaskController(TaskRepository taskRepository,
                          RabbitTemplate rabbitTemplate,
                          RestTemplate restTemplate,
                          @Value("${case.service.url}") String caseServiceUrl) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
        this.caseServiceUrl = caseServiceUrl;
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        if (task.getCaseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo caseId es obligatorio");
        }
        validateCaseExists(task.getCaseId());

        // Por defecto, una tarea nueva está PENDIENTE
        if (task.getStatus() == null) {
            task.setStatus("PENDIENTE");
        }
        Task savedTask = taskRepository.save(task);

        // ¡Punto 6: Publicamos el evento en RabbitMQ!
        String evento = "🚨 [WORKFLOW] Nueva tarea asignada a " + task.getAssignedRole() + ": " + task.getDescription();
        rabbitTemplate.convertAndSend("workflow.queue", evento);

        return savedTask;
    }

    private void validateCaseExists(Long caseId) {
        try {
            restTemplate.getForEntity(caseServiceUrl + "/cases/" + caseId, String.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede crear tarea: el caso con id " + caseId + " no existe");
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al validar el caso en el servicio de casos: " + ex.getStatusCode());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error de conexión con el servicio de casos: " + ex.getMessage());
        }
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Endpoint para que un rol vea solo sus propias tareas
    @GetMapping("/role/{role}")
    public List<Task> getTasksByRole(@PathVariable String role) {
        return taskRepository.findByAssignedRole(role);
    }
}