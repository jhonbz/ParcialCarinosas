package com.carinosas.people_service.controller;

import com.carinosas.people_service.model.Person;
import com.carinosas.people_service.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PersonController {

    private final PersonRepository personRepository;
    private final RestTemplate restTemplate;
    private final String caseServiceUrl;

    public PersonController(PersonRepository personRepository, RestTemplate restTemplate,
                            @Value("${case.service.url}") String caseServiceUrl) {
        this.personRepository = personRepository;
        this.restTemplate = restTemplate;
        this.caseServiceUrl = caseServiceUrl;
    }

    // 1. Crear una persona (POST /people)
    @PostMapping("/people")
    public Person addPerson(@RequestBody Person person) {
        validateCaseExists(person.getCaseId());
        return personRepository.save(person);
    }

    // 2. Obtener todas las personas (GET /people)
    @GetMapping("/people")
    public List<Person> getAllPeople() {
        return personRepository.findAll();
    }

    // 3. Obtener una persona por ID (GET /people/{id})
    @GetMapping("/people/{id}")
    public Person getPersonById(@PathVariable Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Persona con ID " + id + " no encontrada"));
    }

    // 4. Actualizar una persona (PUT /people/{id})
    @PutMapping("/people/{id}")
    public Person updatePerson(@PathVariable Long id, @RequestBody Person updatedPerson) {
        return personRepository.findById(id)
                .map(person -> {
                    person.setName(updatedPerson.getName());
                    person.setDni(updatedPerson.getDni());
                    person.setAge(updatedPerson.getAge());
                    person.setAddress(updatedPerson.getAddress());
                    person.setAlias(updatedPerson.getAlias());
                    person.setRole(updatedPerson.getRole());
                    person.setStatementOrAlibi(updatedPerson.getStatementOrAlibi());
                    person.setContactInfo(updatedPerson.getContactInfo());
                    return personRepository.save(person);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Persona con ID " + id + " no encontrada"));
    }

    // 5. Eliminar una persona (DELETE /people/{id})
    @DeleteMapping("/people/{id}")
    public void deletePerson(@PathVariable Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Persona con ID " + id + " no encontrada");
        }
        personRepository.deleteById(id);
    }

    // 6. Obtener personas por caso (GET /cases/{caseId}/people)
    @GetMapping("/cases/{caseId}/people")
    public List<Person> getPeopleByCaseId(@PathVariable Long caseId) {
        return personRepository.findByCaseId(caseId);
    }

    private void validateCaseExists(Long caseId) {
        try {
            restTemplate.getForEntity(caseServiceUrl + "/cases/" + caseId, String.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede crear persona: el caso con id " + caseId + " no existe");
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al validar el caso en el servicio de casos: " + ex.getStatusCode());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error de conexión con el servicio de casos: " + ex.getMessage());
        }
    }
}