package com.project.back_end.services;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.project.back_end.models.Prescription;

public interface PrescriptionService {
    
    ResponseEntity<Map<String,String>> savePrescription(Prescription prescription);

    ResponseEntity<?> getPrescription(Long appointmentId);

}
