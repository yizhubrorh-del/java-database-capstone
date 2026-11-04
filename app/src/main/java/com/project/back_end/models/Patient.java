package com.project.back_end.models;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@AllArgsConstructor @NoArgsConstructor 
@Builder 
public class Patient {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 100)
    @NotBlank(message = "Name can't be blank")
    private String name;

    @Email 
    @NotBlank(message = "Email can't be blank")
    private String email;

    @Size (min = 6)
    @NotBlank(message = "Password can't be blank")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Phone can't be blank")
    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @NotBlank(message = "Adress can't be blank")
    @Size(max = 255)
    private String address;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth can't be null")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[0-9]{10}$")
    @NotBlank(message = "Emergency contact can't be blank")
    private String emergencyContact;

    @Size(min = 3, max = 30)
    @NotBlank(message = "Insurance Provider can't be blank")
    private String insuranceProvider;
}
