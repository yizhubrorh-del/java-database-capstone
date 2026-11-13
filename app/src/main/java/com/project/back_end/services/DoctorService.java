package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;


public interface DoctorService {

    ResponseEntity<List<String>> getDoctorAvailability(Long doctorId, LocalDate date);

    ResponseEntity<Map<String,String>> saveDoctor(Doctor doctor);

    ResponseEntity<List<Doctor>> getDoctors();
    
    ResponseEntity<Map<String, String>> updateDoctor(Doctor doctor);

    ResponseEntity<Map<String, String>> deleteDoctor(Long doctorId);

    ResponseEntity<Map<String, String>> validateDoctor(Login login);
    
    ResponseEntity<List<Doctor>> findDoctorByName(String name);

    ResponseEntity<List<Doctor>> filterDoctorsByNameSpecialtyAndTime(String name, String speciality, LocalDateTime availableTime);

    ResponseEntity<List<Doctor>> filterDoctorByTime(LocalDateTime start, LocalDateTime end);

    ResponseEntity<List<Doctor>> filterDoctorByNameAndTime(String name, LocalDateTime availableTime);

    ResponseEntity<List<Doctor>> filterDoctorByNameAndSpeciality(String name, String speciality);

    ResponseEntity<List<Doctor>> filterDoctorByTimeAndSpeciality(LocalDateTime availableTime, String speciality);

    ResponseEntity<List<Doctor>> filterDoctorBySpeciality(String speciality);

    ResponseEntity<List<Doctor>> filterDoctors(String name, String specialty, LocalDate start, LocalDate end);
}
