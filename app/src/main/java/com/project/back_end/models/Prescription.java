package com.project.back_end.models;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection = "prescriptions")
@Builder 
@Data
@NoArgsConstructor
@AllArgsConstructor 
public class Prescription {

  @Id
  private String id;

  @NotBlank (message = "Patient name can't be blank")
  @Size(min = 3, max = 100)
  private String patientName;

  @NotNull(message = "Appointment Id can't be null")
  private Long appointmentId;

  @NotBlank(message = "Medication can't be blank")
  @Size(min = 3, max = 100)
  private String medication;

  @NotBlank(message = "Dosage can't be blank")
  private String dosage;

  @Size(max = 200)
  private String doctorNotes;
}
