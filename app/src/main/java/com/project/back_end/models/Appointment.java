package com.project.back_end.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_appointment_doctor_time",
        columnNames = {"doctor_id", "appointment_time"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  // Campos derivados: se calculan al pedirlos, así funcionan también con las citas que Hibernate
  // carga de la BD (que se construyen con el constructor vacío).

  @Transient
  public LocalDateTime getEndTime() {
    return appointmentTime != null ? appointmentTime.plusHours(1) : null;
  }

  @Transient
  public LocalDate getAppointmentDate() {
    return appointmentTime != null ? appointmentTime.toLocalDate() : null;
  }

  @Transient
  public LocalTime getAppointmentTimeOnly() {
    return appointmentTime != null ? appointmentTime.toLocalTime() : null;
  }
}
