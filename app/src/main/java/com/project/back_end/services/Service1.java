package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class Service1 {
    @Autowired
    private final TokenService tokenService;
    @Autowired
    private final PatientService patientService;
    @Autowired
    private final DoctorService doctorService;
    @Autowired
    private final AdminRepository adminRepository;
    @Autowired
    private final DoctorRepository doctorRepository;

// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.
void validateToken(String token, String user) {
        if (!tokenService.validateToken(token, user)) {
            throw new RuntimeException("Invalid or expired token");
        }
    }
// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unau   thorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.
    public ResponseEntity<?> validateAdmin(String username, String password) {
        try {
            var admin = adminRepository.findByUsername(username);
            if (admin != null) {
                if (admin.getPassword().equals(password)) {
                    String token = tokenService.generateToken(admin.getUsername());
                    return ResponseEntity.ok(Map.of("token", token));
                } else {
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Admin not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.
    public List<Doctor> filterDoctor(String name, String specialty, String time) {
        if (name != null && specialty != null && time != null) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time).getBody().get("doctors");
        } else if (name != null && specialty != null) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty).getBody().get("doctors");
        } else if (name != null && time != null) {
            return doctorService.filterDoctorByNameAndTime(name, time).getBody().get("doctors");
        } else if (specialty != null && time != null) {
            return doctorService.filterDoctorsBySpecilityAndTime(specialty, time).getBody().get("doctors");
        } else if (name != null) {
            return doctorService.findDoctorByName(name).getBody().get("doctors");
        } else if (specialty != null) {
            return doctorService.filterDoctorBySpecility(specialty).getBody().get("doctors");
        } else if (time != null) {
            return doctorService.filterDoctorByTime(time).getBody().get("doctors");
        } else {
            return doctorService.getAllDoctors();
        }
    }

    Service1() {
    }
// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.
    public int validateAppointment(Long doctorId, String date, String appointmentTime) {
        var doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return -1; // Doctor not found
        }
        var availableSlots = doctorService.getDoctorAvailability(doctorId, date);
        for (var slot : availableSlots.get("availableTimes")) {
            if (slot.get("startTime").equals(appointmentTime)) {
                return 1; // Valid appointment time
            }
        }
        return 0; // Invalid appointment time
    }

    Service1(PatientService patientService, DoctorService doctorService, TokenService tokenService, DoctorRepository doctorRepository) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.
    public boolean validatePatient(Patient patient) {
        return patientService.validatePatient(patient);
    }

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.
    public ResponseEntity<?> validatePatientLogin(String email, String password) {
        try {
            var patient = patientService.findByEmail(email);
            if (patient != null) {
                if (patient.getPassword().equals(password)) {
                    String token = tokenService.generateToken(patient.getEmail());
                    return ResponseEntity.ok(Map.of("token", token));
                } else {
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Patient not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.
    public List<Appointment> filterPatient(String condition, String doctorName) {
        String email = tokenService.getEmailFromToken(tokenService.getCurrentToken());
        if (condition != null && doctorName != null) {
            return patientService.filterAppointmentsByConditionAndDoctor(email, condition, doctorName);
        } else if (condition != null) {
            return patientService.filterAppointmentsByCondition(email, condition);
        } else if (doctorName != null) {
            return patientService.filterAppointmentsByDoctor(email, doctorName);
        } else {
            return patientService.getPatientAppointments(tokenService.getCurrentToken());
        }
    }
}
