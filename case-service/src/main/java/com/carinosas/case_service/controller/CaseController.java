package com.carinosas.case_service.controller;

import com.carinosas.case_service.model.InvestigationCase;
import com.carinosas.case_service.repository.CaseRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/cases") // Todas las URLs de este archivo empezarán con /cases
public class CaseController {

    private final CaseRepository caseRepository;

    // Inyectamos el repositorio para poder usar la base de datos
    public CaseController(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    // 1. Crear un nuevo caso (POST /cases)
    @PostMapping
    public InvestigationCase createCase(@RequestBody InvestigationCase newCase) {
        // Guarda el caso en la base de datos y lo devuelve con su ID generado
        return caseRepository.save(newCase);
    }

    // 2. Obtener todos los casos (GET /cases)
    @GetMapping
    public List<InvestigationCase> getAllCases() {
        return caseRepository.findAll();
    }

    // 3. Obtener un caso por su ID (GET /cases/{id}) - ¡Requisito del parcial!
    @GetMapping("/{id}")
    public InvestigationCase getCaseById(@PathVariable Long id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Caso con ID " + id + " no encontrado"));
    }
    // 4. Eliminar un caso por su ID (DELETE /cases/{id})
    @DeleteMapping("/{id}")
    public void deleteCase(@PathVariable Long id) {
        caseRepository.deleteById(id);
    }
}