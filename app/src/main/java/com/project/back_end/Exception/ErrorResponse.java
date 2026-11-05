package com.project.back_end.Exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data @Builder 
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Map<String, String> validationErrors;
}
