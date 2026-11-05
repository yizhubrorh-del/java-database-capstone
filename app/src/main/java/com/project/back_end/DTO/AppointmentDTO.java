package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    
    private Long id;

    private Long doctorId;

    private String doctorName;

    private Long patientId;
    
    private String patientName;

    private String patientEmail;

    private String patientPhone;

    private String patientAddress;

    private LocalDateTime appointmentTime;

    private int status;

    private LocalDate appointmentDate;

    private LocalTime appointmentTimeOnly;

    private LocalDateTime endTime;

    public AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId, String patientName, String patientEmail, 
                          String patientPhone, String patientAddress, LocalDateTime appointmentTime, int status){
        
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentDate = appointmentTime != null ? appointmentTime.toLocalDate() : null;
        this.appointmentTimeOnly = appointmentTime != null ? appointmentTime.toLocalTime() : null;
        this.endTime = appointmentTime != null ? appointmentTime.plusHours(1) : null;
                        
                          }
}
