package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class Service1 {
    private final TokenService tokenService;
   private final AdminRepository adminRepository;
    private DoctorRepository doctorRepository;
    PatientService patientService;

    public Service1(TokenService tokenService, AdminRepository adminRepository, DoctorRepository doctorRepository) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
    }

    // 1. **validateToken Method**:
    //    - This method validates a JWT token for a specific user type (e.g., admin, doctor, patient).
    //    - It uses the `TokenService` to check the validity of the token.
    //    - If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message.
    //    - This method is crucial for securing endpoints and ensuring that only authenticated users can access protected resources.
    public ResponseEntity<Map<String, Object>> validateToken(String token, String userType) {
        if (!tokenService.validateToken(token, userType)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of("message", "Token is valid"));
    }

    // 2. **validateAdmin Method**:
    //    - This method validates the login credentials for an admin user.
    //    - It checks if an admin with the provided username exists in the database and if the password matches.
    //    - If the credentials are valid, it generates a JWT token and returns it in the response.
    //    - If the credentials are invalid, it returns a 401 Unauthorized response with an appropriate error message.
    public ResponseEntity<Map<String, Object>> validateAdmin(String username, String password) {
        try {
            var admin = adminRepository.findByUsername(username);
            if (admin != null) {
                if (admin.getPassword().equals(password)) {
                    String token = tokenService.generateToken(admin.getUsername(), "admin");
                    return ResponseEntity.ok(Map.of("token", token));
                } else {
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Admin not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred during admin validation"));
        }
    }

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

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unau   thorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.


// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.
    
// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.


// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.
    public ResponseEntity<Map<String, Object>> validatePatient(String email, String phone) {
        try {
            var patient = doctorRepository.findByEmail(email);
            if (patient != null) {
                return ResponseEntity.ok(Map.of("isValid", false));
            }
            patient = doctorRepository.findByPhone(phone);
            if (patient != null) {
                return ResponseEntity.ok(Map.of("isValid", false));
            }
            return ResponseEntity.ok(Map.of("isValid", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred during patient validation"));
        }
    }

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.
    public ResponseEntity<Map<String, Object>> validatePatientLogin(String email, String password) {
        try {
            var patient = doctorRepository.findByEmail(email);
            if (patient != null) {
                if (patient.getPassword().equals(password)) {
                    String token = tokenService.generateToken(patient.getEmail(), "patient");
                    return ResponseEntity.ok(Map.of("token", token));
                } else {
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Patient not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred during patient login validation"));
        }
    }
// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        String email = tokenService.extractEmailFromToken(token);
        if (condition == null && name == null) {
            return ResponseEntity.ok(Map.of("appointments", patientService.getAllAppointmentsByEmail(email)));
        }
        if (condition != null && name == null) {
            return ResponseEntity.ok(Map.of("appointments", patientService.filterByCondition(condition, email)));
        }
        if (condition == null) {
            return ResponseEntity.ok(Map.of("appointments", patientService.filterByDoctor(Long.valueOf(name), email)));
        }
        return ResponseEntity.ok(Map.of("appointments", patientService.filterByDoctorAndCondition(Long.valueOf(condition), name, email)));
    }
    public ResponseEntity<Map<String, Object>> filterDoctor(String name, String specialty, String time) {
        if (name == null && specialty == null && time == null) {
            return ResponseEntity.ok(Map.of("doctors", doctorRepository.findAll()));
        }
        try {
            List<Doctor> doctors;
            if (name != null && specialty != null && time != null) {
                doctors = doctorRepository.findByNameAndSpecialtyAndTime(name, specialty, time);
            } else if (name != null && specialty != null) {
                doctors = doctorRepository.findByNameAndSpecialty(name, specialty);
            } else if (name != null && time != null) {
                doctors = doctorRepository.findByNameAndTime(name, time);
            } else if (specialty != null && time != null) {
                doctors = doctorRepository.findBySpecialtyAndTime(specialty, time);
            } else if (name != null) {
                doctors = doctorRepository.findByName(name);
            } else if (specialty != null) {
                doctors = doctorRepository.findBySpecialty(specialty);
            } else if (time != null) {
                doctors = doctorRepository.findByAvailableTime(time);
            } else {
                doctors = doctorRepository.findAll();
            }
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors"));
        }
    }

    public ResponseEntity<Map<String, Object>> validateAppointment(String doctorEmail, String date, String time) {
        var doctor = doctorRepository.findByEmail(doctorEmail);
        if (doctor == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Doctor not found"));
        }
        List<String> availableTimes = doctor.getAvailableTimes();
        for (String availableTime : availableTimes) {
            if (availableTime.equals(time)) {
                return ResponseEntity.ok(Map.of("isValid", 1));
            }
        }
        return ResponseEntity.ok(Map.of("isValid", 0));
    }

}
