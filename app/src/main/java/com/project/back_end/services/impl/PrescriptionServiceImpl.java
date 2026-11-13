package com.project.back_end.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import com.project.back_end.services.PrescriptionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class PrescriptionServiceImpl implements PrescriptionService{
    
    private final PrescriptionRepository prescriptionRepository;

    @Override
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        if (prescriptionRepository.existsByAppointmentId(prescription.getAppointmentId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Prescription already exists"));
        }
        prescriptionRepository.save(prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Prescription created"));
    }

    @Override
    public ResponseEntity<?> getPrescription(Long appointmentId) {
        var prescription = prescriptionRepository.findByAppointmentId(appointmentId);
        if (prescription.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("prescription", List.of(prescription.get())));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Prescription not found"));
        }
    }
    
}
