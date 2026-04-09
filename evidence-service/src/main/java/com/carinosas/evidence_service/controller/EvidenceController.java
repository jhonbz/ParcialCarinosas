package com.carinosas.evidence_service.controller;

import com.carinosas.evidence_service.model.Evidence;
import com.carinosas.evidence_service.repository.EvidenceRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class EvidenceController {

    private final EvidenceRepository evidenceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final String caseServiceUrl;

    public EvidenceController(EvidenceRepository evidenceRepository,
                              RabbitTemplate rabbitTemplate,
                              RestTemplate restTemplate,
                              @Value("${case.service.url}") String caseServiceUrl) {
        this.evidenceRepository = evidenceRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
        this.caseServiceUrl = caseServiceUrl;
    }

    @PostMapping("/evidences")
    public Evidence addEvidence(@RequestBody Evidence evidence) {
        if (evidence.getCaseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo caseId es obligatorio");
        }

        validateCaseExists(evidence.getCaseId());

        // 1. Guardamos la evidencia normalmente en la base de datos
        Evidence savedEvidence = evidenceRepository.save(evidence);

        // 2. ¡Lanzamos el evento por la radio a la cola "evidence.queue"!
        String mensaje = "Nueva evidencia agregada para el caso ID: " + evidence.getCaseId();
        rabbitTemplate.convertAndSend("evidence.queue", mensaje);

        // 3. Devolvemos la evidencia guardada
        return savedEvidence;
    }

    private void validateCaseExists(Long caseId) {
        try {
            restTemplate.getForEntity(caseServiceUrl + "/cases/" + caseId, String.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede crear evidencia: el caso con id " + caseId + " no existe");
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al validar el caso en el servicio de casos: " + ex.getStatusCode());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error de conexión con el servicio de casos: " + ex.getMessage());
        }
    }

    // 2. Obtener las evidencias de un caso (Requisito: GET /cases/{id}/evidences)
    @GetMapping({"/evidences/case/{caseId}", "/cases/{caseId}/evidences"})
    public List<Evidence> getEvidencesByCaseId(@PathVariable Long caseId) {
        return evidenceRepository.findByCaseId(caseId);
    }

    // 3. Eliminar una evidencia (Preparación para la regla del ADMIN)
    @DeleteMapping("/evidences/{id}")
    public void deleteEvidence(@PathVariable Long id) {
        evidenceRepository.deleteById(id);
    }
}