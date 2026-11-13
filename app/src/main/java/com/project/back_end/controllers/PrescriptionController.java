package com.project.back_end.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.impl.ServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final ServiceImpl service;
    private final AppointmentService appointmentService;

    @PostMapping("/{token}")
    public ResponseEntity<?> savePrescription(@PathVariable String token, @Valid  @RequestBody Prescription prescription){

        var validation = service.validateToken(token, "doctor");
        if(!validation.getStatusCode().equals(HttpStatus.OK)){
            return validation;
        }

        var response = prescriptionService.savePrescription(prescription);
        if (response.getStatusCode().equals(HttpStatus.CREATED)) {
            appointmentService.changeStatus(prescription.getAppointmentId(), 1);
        }
        return response;

    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId, @PathVariable String token){
        var validation = service.validateToken(token, "doctor");
        if(!validation.getStatusCode().equals(HttpStatus.OK)){
            return validation;
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
