package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    // 2. **Constructor Injection for Dependencies**:
//    - The `DoctorService` class depends on `DoctorRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies should be injected via the constructor for proper dependency management.
//    - Instruction: Ensure constructor injection is used for injecting dependencies into the service.
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private DoctorService doctorService;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

// 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database Data**:
//    - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`, `filterDoctorsBy*` should be annotated with `@Transactional`.
//    - The `@Transactional` annotation ensures that database operations are consistent and wrapped in a single transaction.
//    - Instruction: Add the `@Transactional` annotation above the methods that perform database operations or queries.


    // 4. **getDoctorAvailability Method**:
//    - Retrieves the available time slots for a specific doctor on a particular date and filters out already booked slots.
//    - The method fetches all appointments for the doctor on the given date and calculates the availability by comparing against booked slots.
//    - Instruction: Ensure that the time slots are properly formatted and the available slots are correctly filtered.
    @Transactional
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(Long doctorId, LocalDate date, Object availableSlots) {
        try {
            List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
            // Logic to calculate available slots
            return ResponseEntity.ok(Map.of("availableSlots", availableSlots));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch doctor availability"));
        }
    }

    // 5. **saveDoctor Method**:
//    - Used to save a new doctor record in the database after checking if a doctor with the same email already exists.
//    - If a doctor with the same email is found, it returns `-1` to indicate conflict; `1` for success, and `0` for internal errors.
//    - Instruction: Ensure that the method correctly handles conflicts and exceptions when saving a doctor.
    public boolean saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return false; // Doctor with the same email already exists
            }
            doctorRepository.save(doctor);
            return true; // Doctor saved successfully
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Internal error occurred
        }
    }

    // 6. **updateDoctor Method**:
//    - Updates an existing doctor's details in the database. If the doctor doesn't exist, it returns `-1`.
//    - Instruction: Make sure that the doctor exists before attempting to save the updated record and handle any errors properly.
    public boolean updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return false; // Doctor not found
            }
            doctorRepository.save(doctor);
            return true; // Doctor updated successfully
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Internal error occurred
        }
    }

    // 7. **getDoctors Method**:
//    - Fetches all doctors from the database. It is marked with `@Transactional` to ensure that the collection is properly loaded.
//    - Instruction: Ensure that the collection is eagerly loaded, especially if dealing with lazy-loaded relationships (e.g., available times). 
    @Transactional
    public ResponseEntity<Map<String, Object>> getDoctors() {
        try {
            List<Doctor> doctors = doctorRepository.findAll();
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch doctors"));
        }
    }

    // 8. **deleteDoctor Method**:
//    - Deletes a doctor from the system along with all appointments associated with that doctor.
//    - It first checks if the doctor exists. If not, it returns `-1`; otherwise, it deletes the doctor and their appointments.
//    - Instruction: Ensure the doctor and their appointments are deleted properly, with error handling for internal issues.
    public boolean deleteDoctor(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return false; // Doctor not found
            }
            appointmentRepository.deleteByDoctorId(id); // Delete associated appointments
            doctorRepository.deleteById(id); // Delete the doctor
            return true; // Doctor deleted successfully
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Internal error occurred
        }
    }

    // 9. **validateDoctor Method**:
//    - Validates a doctor's login by checking if the email and password match an existing doctor record.
//    - It generates a token for the doctor if the login is successful, otherwise returns an error message.
//    - Instruction: Make sure to handle invalid login attempts and password mismatches properly with error responses.
    public Map<String, Object> validateDoctor(Login login) {
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getEmail());
            if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
                return Map.of("error", "Invalid email or password");
            }
            String token = tokenService.generateToken(String.valueOf(doctor.getId()), "doctor");
            return Map.of("message", "Login successful", "token", token);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "An error occurred during login");
        }
    }

    // 10. **findDoctorByName Method**:
//    - Finds doctors based on partial name matching and returns the list of doctors with their available times.
//    - This method is annotated with `@Transactional` to ensure that the database query and data retrieval are properly managed within a transaction.
//    - Instruction: Ensure that available times are eagerly loaded for the doctors.
    @Transactional
    public ResponseEntity<Map<String, Object>> findDoctorByName(String name) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameLike("%" + name + "%");
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch doctors by name"));
        }
    }

    // 11. **filterDoctorsByNameSpecilityandTime Method**:
//    - Filters doctors based on their name, specialty, and availability during a specific time (AM/PM).
//    - The method fetches doctors matching the name and specialty criteria, then filters them based on their availability during the specified time period.
//    - Instruction: Ensure proper filtering based on both the name and specialty as well as the specified time period.
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameAndSpecialtyAndTime(name, specialty, time);
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors"));
        }
    }

    // 12. **filterDoctorByTime Method**:
//    - Filters a list of doctors based on whether their available times match the specified time period (AM/PM).
//    - This method processes a list of doctors and their available times to return those that fit the time criteria.
//    - Instruction: Ensure that the time filtering logic correctly handles both AM and PM time slots and edge cases.
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorByTime(String time) {
        try {
            List<Doctor> doctors = doctorRepository.findByAvailableTime(time);
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors by time"));
        }
    }

    // 13. **filterDoctorByNameAndTime Method**:
//    - Filters doctors based on their name and the specified time period (AM/PM).
//    - Fetches doctors based on partial name matching and filters the results to include only those available during the specified time period.
//    - Instruction: Ensure that the method correctly filters doctors based on the given name and time of day (AM/PM).
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorByNameAndTime(String name, String time) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameAndTime(name, time);
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors by name and time"));
        }
    }

    // 14. **filterDoctorByNameAndSpecility Method**:
//    - Filters doctors by name and specialty.
//    - It ensures that the resulting list of doctors matches both the name (case-insensitive) and the specified specialty.
//    - Instruction: Ensure that both name and specialty are considered when filtering doctors. Method:
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorByNameAndSpecility(String name, String specialty) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors by name and specialty"));
        }
    }

    // 15. **filterDoctorByTimeAndSpecility Method**:
//    - Filters doctors based on their specialty and availability during a specific time period (AM/PM).
//    - Fetches doctors based on the specified specialty and filters them based on their available time slots for AM/PM.
//    - Instruction: Ensure the time filtering is accurately applied based on the given specialty and time period (AM/PM).
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorByTimeAndSpecility(String time, String specialty) {
        try {
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            List<Doctor> filteredDoctors = doctors.stream()
                    .filter(doctor -> doctor.getAvailableTimes().stream()
                            .anyMatch(availableTime -> availableTime.equalsIgnoreCase(time)))
                    .toList();
            return ResponseEntity.ok(Map.of("doctors", filteredDoctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors by time and specialty"));
        }
    }

    // 16. **filterDoctorBySpecility Method**:
//    - Filters doctors based on their specialty.
//    - This method fetches all doctors matching the specified specialty and returns them.
//    - Instruction: Make sure the filtering logic works for case-insensitive specialty matching.
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorBySpecility(String specialty) {
        try {
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors by specialty"));
        }
    }

    // 17. **filterDoctorsByTime Method**:
//    - Filters all doctors based on their availability during a specific time period (AM/PM).
//    - The method checks all doctors' available times and returns those available during the specified time period.
//    - Instruction: Ensure proper filtering logic to handle AM/PM time periods.
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctorsByTime(String time) {
        try {
            List<Doctor> doctors = doctorRepository.findAll();
            List<Doctor> filteredDoctors = doctors.stream()
                    .filter(doctor -> doctor.getAvailableTimes().stream()
                            .anyMatch(availableTime -> availableTime.equalsIgnoreCase(time)))
                    .toList();
            return ResponseEntity.ok(Map.of("doctors", filteredDoctors));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to filter doctors by time"));
        }
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Map<String, Object> doctorLogin(Login login) {
        return doctorService.validateDoctor(login);
    }
}
