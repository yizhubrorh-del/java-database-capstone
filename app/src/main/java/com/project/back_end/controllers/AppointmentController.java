package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ServiceImpl service;
    private final TokenServiceImpl tokenService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @PathVariable String patientName,
        @PathVariable String token){
        
        var validation = service.validateToken(token, "doctor");
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }

        String email = tokenService.extractEmail(token);
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
        }

        String name = "null".equalsIgnoreCase(patientName) ? null : patientName;

        var appointments = appointmentService.getAppointments(doctor.getId(), date, name).getBody();
        return ResponseEntity.ok(Map.of("appointments", appointments));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token,
                                                               @Valid @RequestBody AppointmentDTO appointment){

        var validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }

        var result = appointmentService.bookAppointment(appointment);
        HttpStatusCode status = result.getStatusCode();

        String message;
        if (status.equals(HttpStatus.CREATED)) {
            message = "Appointment booked successfully";
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            message = "Invalid doctor id";
        } else if (status.equals(HttpStatus.CONFLICT)) {
            message = "Appointment time unavailable";
        } else {
            message = "Could not book the appointment";
        }

        return ResponseEntity.status(status).body(Map.of("message", message));
    }


    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@PathVariable String token,
                                                                 @Valid @RequestBody AppointmentDTO appointment){

        var validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }

        // The patient in the body must be the same one that owns the token
        Patient patient = patientRepository.findByEmail(tokenService.extractEmail(token));
        if (patient == null || !patient.getId().equals(appointment.getPatientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(Map.of("message", "The appointment doesn't belong to the user"));
        }

        return toMessageResponse(appointmentService.updateAppointment(appointment));
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id,
                                                                 @PathVariable String token){

        var validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }

        return toMessageResponse(appointmentService.cancelAppointment(id, token));
    }

    // Keeps the status of the service response and guarantees a "message" key for the frontend
    private ResponseEntity<Map<String, String>> toMessageResponse(ResponseEntity<Map<String, String>> response){
        Map<String, String> body = response.getBody();
        String message = "Something went wrong";
        if (body != null) {
            message = body.getOrDefault("message", message);
        }
        return ResponseEntity.status(response.getStatusCode()).body(Map.of("message", message));
    }

}
