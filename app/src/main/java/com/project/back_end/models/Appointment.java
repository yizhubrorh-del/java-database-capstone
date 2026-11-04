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
import jakarta.validation.constraints.NotNull;

@Entity 
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

  @Future(message = "Appointment time must be in the future")
  private LocalDateTime appointmentTime;

  @NotNull(message = "Status can't be null")
  private int status;

  @Transient
  public LocalDateTime getEndTime(){
    if(this.appointmentTime == null){
      return null;
    }
    return this.appointmentTime.plusHours(1);
  }

  @Transient 
  public LocalDate getAppointmentDate(){
    return this.appointmentTime.toLocalDate();
  }

  @Transient 
  public LocalTime getAppointmentTimeOnly(){
    return this.appointmentTime.toLocalTime();
  }
}

