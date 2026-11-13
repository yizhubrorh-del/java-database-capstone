package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.impl.ServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final ServiceImpl service;

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token){
        var validation = service.validateToken(token, "patient");
        if(!validation.getStatusCode().equals(HttpStatus.OK)){
            return validation;
        }

        var response = patientService.getPatientDetails(token);
        
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("patient", response.getBody()));
    }

    @PostMapping()
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient){
        boolean isNew = service.validatePatient(patient.getEmail(), patient.getPhone()).getBody();
        if (!isNew) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Map.of("message", "Patient with email id or phone no already exist"));
        }

        try {
            patientService.createPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Signup successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Internal server error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Login login){
        return service.validatePatientLogin(login);
    }


    // The frontend sends the role of the caller: a patient sees their own appointments,
    // a doctor opens the patient record from the doctor dashboard
    @GetMapping("/{id}/{user}/{token}")
    public ResponseEntity<?> getPatientAppointment(@PathVariable Long id,
                                                   @PathVariable String user,
                                                   @PathVariable String token){
        var validation = service.validateToken(token, user);
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }

        // A patient can only read their own appointments
        if (user.equalsIgnoreCase("patient")
                && !patientService.getPatientDetails(token).getBody().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(Map.of("message", "You can only see your own appointments"));
        }

        var response = patientService.getPatientAppointment(id);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("appointments", response.getBody()));
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(@PathVariable String condition,
                                                      @PathVariable String name,
                                                      @PathVariable String token
    ){
        var validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }
        
        Integer status = switch(condition.toLowerCase()){
            case "future" -> 0;
            case "past" -> 1;
            default -> null;
        };
        String doctorName = "null".equalsIgnoreCase(name) ? null : name;

        var appointments = service.filterPatient(token, doctorName, status).getBody();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("appointments", appointments));
    }
}


