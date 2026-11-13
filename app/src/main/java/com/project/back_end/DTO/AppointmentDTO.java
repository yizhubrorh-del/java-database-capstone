package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppointmentDTO {
    
    private Long id;

    @NotNull(message = "Doctor id can't be null")
    private Long doctorId;

    private String doctorName;

    @NotNull(message = "Patient id can't be null")
    private Long patientId;
    
    private String patientName;

    private String patientEmail;

    private String patientPhone;

    private String patientAddress;

    @NotNull(message = "Appointment time can't be null")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;

    @Min(value = 0, message = "Status must be 0 (scheduled) or 1 (completed)")
    @Max(value = 1, message = "Status must be 0 (scheduled) or 1 (completed)")
    private int status;

    @NotBlank(message = "The visit reason can't be blank")
    @Size(min = 6, max = 50)
    private String reasonForVisit;

    @Size(max = 100)
    private String notes;


    private LocalDate appointmentDate;

    private LocalTime appointmentTimeOnly;

    private LocalDateTime endTime;

    @Builder 
    public AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId, String patientName, String patientEmail, 
                          String patientPhone, String patientAddress, LocalDateTime appointmentTime, int status, String reasonForVisit, String notes){
        
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reasonForVisit = reasonForVisit;
        this.notes = notes;
        this.appointmentDate = appointmentTime != null ? appointmentTime.toLocalDate() : null;
        this.appointmentTimeOnly = appointmentTime != null ? appointmentTime.toLocalTime() : null;
        this.endTime = appointmentTime != null ? appointmentTime.plusHours(1) : null;
                        
                          }
}
