package com.project.back_end.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor @NoArgsConstructor 
public class Doctor {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name can't be blank")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "Specialty can't be blank")
    @Size(min = 3, max = 50)
    private String speciality;

    @NotBlank(message = "Email can't be null")
    @Email(message = "Email must follow the pattern: mail@domain.com")
    private String email;

    @NotBlank(message = "Password can't be blank")
    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Phone can't be blank")
    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @ElementCollection 
    private List<String> availableTimes;

    @NotNull(message = "Years of experience can't be null")
    @Min(value = 0, message = "Years of experience can't be negative") 
    private int yearsOfExperience;

    @NotBlank(message = "Clinic Adress can't be blank")
    @Size(max = 255)
    private String clinicAdress;

    @NotNull(message = "Rating can't be null")
    @Min(value = 0, message = "Rating can't be negative")
    @Max(value = 5, message = "The max rating is 5")
    private Double rating;




}

