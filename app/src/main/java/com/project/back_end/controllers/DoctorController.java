package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.impl.ServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@RequestMapping({"${api.path}doctor", "/api/doctor"})
public class DoctorController {
    private final DoctorService doctorService;
    private final ServiceImpl service;

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(@PathVariable("user") String role,
                                                   @PathVariable Long doctorId,
                                                   @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                   @PathVariable String token){
        var validation = service.validateToken(token, role);
        if (!validation.getStatusCode().equals(HttpStatus.OK)){
            return validation;
        }

        var availability = doctorService.getDoctorAvailability(doctorId, date).getBody();
        return ResponseEntity.ok(Map.of("availability", availability));
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Doctor>>> getDoctors(){
        return ResponseEntity.ok(Map.of("doctors", doctorService.getDoctors().getBody()));
    }

    @PostMapping({"/{token}", "/save/{token}"})
    public ResponseEntity<Map<String, String>> saveDoctor(@PathVariable String token, @Valid @RequestBody Doctor doctor){
        var validation = service.validateToken(token, "admin");
        if(!validation.getStatusCode().equals(HttpStatus.OK)){
            return validation;
        }

        return doctorService.saveDoctor(doctor);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@Valid @RequestBody Login login){
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@PathVariable String token, @Valid @RequestBody Doctor doctor){
        var validation = service.validateToken(token, "admin");
        if (!validation.getStatusCode().equals(HttpStatus.OK)) {
            return validation;
        }

        return doctorService.updateDoctor(doctor);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, @PathVariable String token){
        var validation = service.validateToken(token, "admin");
        if(!validation.getStatusCode().equals(HttpStatus.OK)){
            return validation;
        }

        return doctorService.deleteDoctor(id);
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, List<Doctor>>> filter(@PathVariable String name,
                                                            @PathVariable String time,
                                                            @PathVariable String speciality){
        // The frontend sends the string "null" for empty filters
        var doctors = service.filterDoctor(nullIfEmpty(name), nullIfEmpty(time), nullIfEmpty(speciality)).getBody();
        return ResponseEntity.ok(Map.of("doctors", doctors));
    }

    private String nullIfEmpty(String value){
        return value == null || value.isBlank() || value.equalsIgnoreCase("null") ? null : value;
    }

}
