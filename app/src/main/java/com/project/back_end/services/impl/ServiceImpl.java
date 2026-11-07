package com.project.back_end.services.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.PatientService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class ServiceImpl {
    private final TokenServiceImpl tokenService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public ResponseEntity<Map<String, String>> validateToken(String token, String role){
        if(!tokenService.validateToken(token, role)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of("message", "Token is valid"));
    }
    
    public ResponseEntity<Map<String, String>> validateAdmin(String username, String pwd){
        var admin = adminRepository.findByUsername(username);

        if (admin != null && admin.getPassword().equals(pwd)) {
            String token = tokenService.generateToken(username, "admin");
            return ResponseEntity.status(HttpStatus.OK)
                                 .body(Map.of("token", token));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Incorrect password or username"));
        }
    }

    public ResponseEntity<List<Doctor>> filterDoctor(String name, String specialty, LocalDate start, LocalDate end){
        return doctorService.filterDoctors(name, specialty, start, end);
    }

    public ResponseEntity<Integer> validateAppointment(AppointmentDTO appointment){
        var doctor = doctorRepository.findById(appointment.getDoctorId());
        if (doctor.isPresent()) {
            String start = appointment.getAppointmentTime().toLocalTime()
                                      .format(DateTimeFormatter.ofPattern("HH:mm"));

            var availableTimeSlots = doctorService.getDoctorAvailability(appointment.getDoctorId(), appointment.getAppointmentDate()).getBody();

            boolean available = availableTimeSlots.stream()
                                                  .anyMatch(slot -> slot.startsWith(start + "-"));
            if (available) {
                return ResponseEntity.status(HttpStatus.OK).body(1);
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(0);
            }
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-1);
        }
    }

    public ResponseEntity<Boolean> validatePatient(String email, String phone){
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("At least an email or a phone must be provided");
        }
        var exists = patientRepository.existsByEmailOrPhone(email, phone);
        
        return ResponseEntity.status(HttpStatus.OK).body(!exists);
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login){
        var patient = patientRepository.findByEmail(login.getEmail());
        if (patient != null && patient.getPassword().equals(login.getPassword())) {
            var token = tokenService.generateToken(patient.getEmail(), "patient");
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("token", token));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect email or password"));
        } 
    }

    public ResponseEntity<List<AppointmentDTO>> filterPatient(String token, String doctorName, Integer condition){
        Patient patient = patientService.getPatientDetails(token).getBody();
        boolean hasDoctorName = doctorName != null && !doctorName.isBlank();
        boolean hasCondition = condition != null;
        List<AppointmentDTO> appointmentDTOs;
        if(hasDoctorName && hasCondition){
            appointmentDTOs = patientService.filterByDoctorAndCondition(doctorName, patient.getId(), condition).getBody();
        } else if(hasDoctorName){
            appointmentDTOs = patientService.filterByDoctor(doctorName, patient.getId()).getBody();
        }else if(hasCondition){
            appointmentDTOs = patientService.filterByCondition(condition, patient.getId()).getBody();
        }else{
            appointmentDTOs = patientService.getPatientAppointment(patient.getId()).getBody();
        }
        return ResponseEntity.status(HttpStatus.OK).body(appointmentDTOs);
    }
}
