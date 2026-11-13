package com.project.back_end.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.AppointmentDTO;

public interface AppointmentService {
    ResponseEntity<Integer> bookAppointment(AppointmentDTO appointment);

    ResponseEntity<Map<String,String>> updateAppointment(AppointmentDTO appointmentDTO);

    ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, String token);

    ResponseEntity<List<AppointmentDTO>> getAppointments(Long doctorId, LocalDate date, String patientName);

    ResponseEntity<Map<String, String>> changeStatus(Long appointmentId, Integer newStatus);
}
