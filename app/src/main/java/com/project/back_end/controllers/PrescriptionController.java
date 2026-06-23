package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service1;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("${api.path}prescription")` to set the base path for all prescription-related endpoints.
//    - This controller manages creating and retrieving prescriptions tied to appointments.


    // 2. Autowire Dependencies:
//    - Inject `PrescriptionService` to handle logic related to saving and fetching prescriptions.
//    - Inject the shared `Service` class for token validation and role-based access control.
//    - Inject `AppointmentService` to update appointment status after a prescription is issued.
    private final PrescriptionService prescriptionService;
    private final Service1 service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService, Service1 service, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    // 3. Define the `savePrescription` Method:
//    - Handles HTTP POST requests to save a new prescription for a given appointment.
//    - Accepts a validated `Prescription` object in the request body and a doctor’s token as a path variable.
//    - Validates the token for the `"doctor"` role.
//    - If the token is valid, updates the status of the corresponding appointment to reflect that a prescription has been added.
//    - Delegates the saving logic to `PrescriptionService` and returns a response indicating success or failure.
    @PostMapping
    public ResponseEntity<Map<String, Object>> savePrescription(@RequestBody @Valid Prescription prescription, @RequestHeader String token) {
        if (service.validateToken(token, "doctor")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        boolean isSaved = prescriptionService.savePrescription(prescription).hasBody();
        if (isSaved) {
            return ResponseEntity.ok(Map.of("message", "Prescription saved successfully"));
        } else {
            return ResponseEntity.status(409).body(Map.of("error", "Failed to save prescription"));
        }
    }

    // 4. Define the `getPrescription` Method:
//    - Handles HTTP GET requests to retrieve a prescription by its associated appointment ID.
//    - Accepts the appointment ID and a doctor’s token as path variables.
//    - Validates the token for the `"doctor"` role using the shared service.
//    - If the token is valid, fetches the prescription using the `PrescriptionService`.
//    - Returns the prescription details or an appropriate error message if validation fails.
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable String appointmentId, @RequestHeader String token) {
        if (service.validateToken(token, "doctor")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        ResponseEntity<Map<String, Prescription>> prescription = prescriptionService.getPrescription(appointmentId);
        if (prescription != null) {
            return ResponseEntity.ok(Map.of("prescription", prescription));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Prescription not found"));
        }
    }
}