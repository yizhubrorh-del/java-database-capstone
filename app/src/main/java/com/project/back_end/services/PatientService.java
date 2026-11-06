package com.project.back_end.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Patient;

public interface PatientService {

    ResponseEntity<String> createPatient(Patient patient);

    ResponseEntity<List<AppointmentDTO>> getPatientAppointment(Long patientId);

    ResponseEntity<List<AppointmentDTO>> filterByCondition(int status, Long patiendId);

    ResponseEntity<List<AppointmentDTO>> filterByDoctor(String doctorName, Long patientId);

    ResponseEntity<List<AppointmentDTO>> filterByDoctorAndCondition(String doctorName, Long patientId, int status);

    ResponseEntity<Patient> getPatientDetails(String token);
}
