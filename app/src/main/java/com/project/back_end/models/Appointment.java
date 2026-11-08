package com.project.back_end.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @NotNull(message = "Doctor can't be null")
  private Doctor doctor;

  @ManyToOne
  @NotNull(message = "Patient can't be null")
  private Patient patient;

  @NotBlank(message = "The visit reason can't be blank")
  @Size(min = 6, max = 50)
  private String reasonForVisit;

  @Future(message = "Appointment time must be in the future")
  private LocalDateTime appointmentTime;

  @NotNull(message = "Status can't be null")
  private int status;

  @Size(max = 100)
  private String notes;

  @Transient 
  private LocalDateTime endTime;
  
  @Transient 
  private LocalDate appointmentDate;

  @Transient 
  private LocalTime appointmentTimeOnly;

  @Builder 
  public Appointment(Long id, Doctor doctor, Patient patient, String reasonForVisit, LocalDateTime appointmentTime, int status, String notes){
    this.id = id;
    this.doctor = doctor;
    this.patient = patient;
    this.reasonForVisit = reasonForVisit;
    this.appointmentTime = appointmentTime;
    this.appointmentDate = appointmentTime != null ? appointmentTime.toLocalDate() : null;
    this.appointmentTimeOnly = appointmentTime != null ? appointmentTime.toLocalTime() : null;
    this.endTime = appointmentTime != null ? appointmentTime.plusHours(1) : null;
  }
}
