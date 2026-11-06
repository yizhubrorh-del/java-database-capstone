package com.project.back_end.services.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.PatientService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PatientServiceImpl implements PatientService{
    
    private final TokenServiceImpl tokenService;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional 
    public ResponseEntity<String> createPatient(Patient patient) {
        patientRepository.save(patient);
        return ResponseEntity.ok("The patient named " + patient.getName() + " was added correctly.");
    }

    @Override
    @Transactional 
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointment(Long patientId) {
        var appointments = appointmentRepository.findByPatientId(patientId).stream()
                                                .map(appointment -> toAppointmentDTO(appointment))
                                                .toList();

        return ResponseEntity.ok(appointments);
    }

    @Override
    @Transactional 
    public ResponseEntity<List<AppointmentDTO>> filterByCondition(int status, Long patientId) {
        if (status != 0 && status != 1 && status != 2) {
            throw new IllegalArgumentException("Status must be 0 (future), 1 (past), 2 (canceled)");
        }
        var appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status).stream()
                                                .map(appointment -> toAppointmentDTO(appointment))
                                                .toList();

        return ResponseEntity.ok(appointments);
    }

    @Override
    @Transactional 
    public ResponseEntity<List<AppointmentDTO>> filterByDoctor(String doctorName, Long patientId) {
        var appointments = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId).stream()
                                                .map(appointment -> toAppointmentDTO(appointment))
                                                .toList();

        return ResponseEntity.ok(appointments);
    }

    @Override
    @Transactional 
    public ResponseEntity<List<AppointmentDTO>> filterByDoctorAndCondition(String doctorName, Long patientId,
            int status) {
        if (status != 0 && status != 1 && status != 2) {
            throw new IllegalArgumentException("Status must be 0 (future), 1 (past), 2 (canceled)");
        }
        var appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status).stream()
                                                .map(this::toAppointmentDTO)
                                                .toList();

        return ResponseEntity.ok(appointments);
    }

    @Override
    @Transactional 
    public ResponseEntity<Patient> getPatientDetails(String token) {
        var patient = patientRepository.findByEmail(tokenService.extractEmail(token));
        if (patient == null) {
            throw new EntityNotFoundException("Patient not found");
        }
        return ResponseEntity.ok(patient);
    }

    // auxiliar methods

    private AppointmentDTO toAppointmentDTO(Appointment appointment){
        return AppointmentDTO.builder()
                             .id(appointment.getId())
                             .doctorId(appointment.getDoctor().getId())
                             .doctorName(appointment.getDoctor().getName())
                             .patientId(appointment.getPatient().getId())
                             .patientName(appointment.getPatient().getName())
                             .patientEmail(appointment.getPatient().getEmail())
                             .patientPhone(appointment.getPatient().getPhone())
                             .patientAddress(appointment.getPatient().getAddress())
                             .appointmentTime(appointment.getAppointmentTime())
                             .status(appointment.getStatus())
                             .build();
    }
    
}
