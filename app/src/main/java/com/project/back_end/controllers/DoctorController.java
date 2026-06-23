package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service1;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST controller that serves JSON responses.
//    - Use `@RequestMapping("${api.path}doctor")` to prefix all endpoints with a configurable API path followed by "doctor".
//    - This class manages doctor-related functionalities such as registration, login, updates, and availability.


    // 2. Autowire Dependencies:
//    - Inject `DoctorService` for handling the core logic related to doctors (e.g., CRUD operations, authentication).
//    - Inject the shared `Service` class for general-purpose features like token validation and filtering.
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private Service1 service;
    private Object availableSlots;

    DoctorController(DoctorService doctorService, Service1 service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // 3. Define the `getDoctorAvailability` Method:
//    - Handles HTTP GET requests to check a specific doctor’s availability on a given date.
//    - Requires `user` type, `doctorId`, `date`, and `token` as path variables.
//    - First validates the token against the user type.
//    - If the token is invalid, returns an error response; otherwise, returns the availability status for the doctor.
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user, @PathVariable Long doctorId, @PathVariable String date, @PathVariable String token) {
        if (service.validateToken(token, user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        Map<String, Object> availability = (Map<String, Object>) doctorService.getDoctorAvailability(doctorId, LocalDate.parse(date), availableSlots);
        return ResponseEntity.ok(availability);
    }

    // 4. Define the `getDoctor` Method:
//    - Handles HTTP GET requests to retrieve a list of all doctors.
//    - Returns the list within a response map under the key `"doctors"` with HTTP 200 OK status.
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(Map.of("doctors", doctors));
    }

    // 5. Define the `saveDoctor` Method:
//    - Handles HTTP POST requests to register a new doctor.
//    - Accepts a validated `Doctor` object in the request body and a token for authorization.
//    - Validates the token for the `"admin"` role before proceeding.
//    - If the doctor already exists, returns a conflict response; otherwise, adds the doctor and returns a success message.
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveDoctor(@RequestBody @Valid Doctor doctor, @RequestHeader String token) {
        if (service.validateToken(token, "admin")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        boolean isSaved = doctorService.saveDoctor(doctor);
        if (isSaved) {
            return ResponseEntity.ok(Map.of("message", "Doctor registered successfully"));
        } else {
            return ResponseEntity.status(409).body(Map.of("error", "Doctor already exists"));
        }
    }

    // 6. Define the `doctorLogin` Method:
//    - Handles HTTP POST requests for doctor login.
//    - Accepts a validated `Login` DTO containing credentials.
//    - Delegates authentication to the `DoctorService` and returns login status and token information.
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(@RequestBody Login login) {
        Map<String, Object> response = doctorService.doctorLogin(login);
        if (response.containsKey("error")) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 7. Define the `updateDoctor` Method:
//    - Handles HTTP PUT requests to update an existing doctor's information.
//    - Accepts a validated `Doctor` object and a token for authorization.
//    - Token must belong to an `"admin"`.
//    - If the doctor exists, updates the record and returns success; otherwise, returns not found or error messages.
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateDoctor(@RequestBody @Valid Doctor doctor, @RequestHeader String token) {
        if (service.validateToken(token, "admin")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        boolean isUpdated = doctorService.updateDoctor(doctor);
        if (isUpdated) {
            return ResponseEntity.ok(Map.of("message", "Doctor updated successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
        }
    }

    // 8. Define the `deleteDoctor` Method:
//    - Handles HTTP DELETE requests to remove a doctor by ID.
//    - Requires both doctor ID and an admin token as path variables.
//    - If the doctor exists, deletes the record and returns a success message; otherwise, responds with a not found or error message.
    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Long doctorId, @RequestHeader String token) {
        if (service.validateToken(token, "admin")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        boolean isDeleted = doctorService.deleteDoctor(doctorId);
        if (isDeleted) {
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
        }
    }

    // 9. Define the `filter` Method:
//    - Handles HTTP GET requests to filter doctors based on name, time, and specialty.
//    - Accepts `name`, `time`, and `speciality` as path variables.
//    - Calls the shared `Service` to perform filtering logic and returns matching doctors in the response.
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String speciality) {
        List<Doctor> filteredDoctors = (List<Doctor>) service.filterDoctor(name, time, speciality);
        return ResponseEntity.ok(Map.of("doctors", filteredDoctors));

    }
}