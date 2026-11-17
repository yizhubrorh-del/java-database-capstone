package com.project.back_end.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ValidationFailedTest {

    @Test
    void handleValidationExceptionReturnsBadRequestWithMessage() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "login");
        bindingResult.addError(new FieldError("login", "email", "Email must be valid"));
        var ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        var response = new ValidationFailed().handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Email must be valid");
    }
}
