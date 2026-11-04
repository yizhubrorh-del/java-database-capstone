package com.project.back_end.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Pharmacy {
    
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String location;
}
