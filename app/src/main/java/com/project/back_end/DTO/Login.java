package com.project.back_end.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor 
@Data 
public class Login {

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password can't be blank")
    @Size(min = 6)
    private String password;
}
