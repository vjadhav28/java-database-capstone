package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service1;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller for patient-related operations.
//    - Use `@RequestMapping("/patient")` to prefix all endpoints with `/patient`, grouping all patient functionalities under a common route.


    // 2. Autowire Dependencies:
//    - Inject `PatientService` to handle patient-specific logic such as creation, retrieval, and appointments.
//    - Inject the shared `Service` class for tasks like token validation and login authentication.
    private final PatientService patientService;
    private final Service1 service;

    public PatientController(PatientService patientService, Service1 service) {
        this.patientService = patientService;
        this.service = service;
    }

    // 3. Define the `getPatient` Method:
//    - Handles HTTP GET requests to retrieve patient details using a token.
//    - Validates the token for the `"patient"` role using the shared service.
//    - If the token is valid, returns patient information; otherwise, returns an appropriate error message.
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPatient(@RequestHeader String token) {
        if (service.validateToken(token, "patient")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        Patient patient = patientService.getPatient(token);
        return ResponseEntity.ok(Map.of("patient", patient));
    }

    // 4. Define the `createPatient` Method:
//    - Handles HTTP POST requests for patient registration.
//    - Accepts a validated `Patient` object in the request body.
//    - First checks if the patient already exists using the shared service.
//    - If validation passes, attempts to create the patient and returns success or error messages based on the outcome.
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody @Valid Patient patient, @RequestHeader String token) {
        if (service.validateToken(token, "patient")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        boolean isCreated = patientService.createPatient(patient);
        if (isCreated) {
            return ResponseEntity.ok(Map.of("message", "Patient created successfully"));
        } else {
            return ResponseEntity.status(409).body(Map.of("error", "Patient already exists"));
        }
    }

    // 5. Define the `login` Method:
//    - Handles HTTP POST requests for patient login.
//    - Accepts a `Login` DTO containing email/username and password.
//    - Delegates authentication to the `validatePatientLogin` method in the shared service.
//    - Returns a response with a token or an error message depending on login success.
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid Login login) {
        Map<String, Object> response = (Map<String, Object>) service.validatePatientLogin(login);
        if (response.containsKey("error")) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 6. Define the `getPatientAppointment` Method:
//    - Handles HTTP GET requests to fetch appointment details for a specific patient.
//    - Requires the patient ID, token, and user role as path variables.
//    - Validates the token using the shared service.
//    - If valid, retrieves the patient's appointment data from `PatientService`; otherwise, returns a validation error.
    @GetMapping("/appointment")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@RequestHeader String token) {
        if (service.validateToken(token, "patient")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        List<Appointment> appointments = patientService.getPatientAppointments(token);
        return ResponseEntity.ok(Map.of("appointments", appointments));
    }

    // 7. Define the `filterPatientAppointment` Method:
//    - Handles HTTP GET requests to filter a patient's appointments based on specific conditions.
//    - Accepts filtering parameters: `condition`, `name`, and a token.
//    - Token must be valid for a `"patient"` role.
//    - If valid, delegates filtering logic to the shared service and returns the filtered result.
    @GetMapping("/appointment/filter")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String name,
            @RequestHeader String token) {
        if (service.validateToken(token, "patient")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        List<Appointment> filteredAppointments = service.filterPatientAppointments(condition, name, token);
        return ResponseEntity.ok(Map.of("appointments", filteredAppointments));
    }
}