package com.project.back_end.Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Each handler maps its exception to the expected status and ErrorResponse body.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void entityNotFoundMapsTo404() {
        var response = handler.handleNotFound(new EntityNotFoundException("Doctor not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Doctor not found");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void illegalArgumentMapsTo400() {
        var response = handler.handleBadRequest(new IllegalArgumentException("Time must be AM or PM"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Time must be AM or PM");
    }

    @Test
    void jwtExceptionMapsTo401() {
        var response = handler.handleJwt(new MalformedJwtException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid or expired token");
    }

    @Test
    void unreadableBodyMapsTo400() {
        var ex = new HttpMessageNotReadableException("bad json", new MockHttpInputMessage(new byte[0]));

        var response = handler.handleUnreadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("The request body is malformed or has invalid values");
    }

    @Test
    void typeMismatchMapsTo400WithParameterName() {
        var ex = new MethodArgumentTypeMismatchException("abc", Long.class, "id", mock(MethodParameter.class), null);

        var response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid value for parameter 'id'");
    }

    @Test
    void unexpectedExceptionMapsTo500() {
        var response = handler.handleGeneral(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
    }

    @Test
    void validationErrorsAreCollectedPerField() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "doctor");
        bindingResult.addError(new FieldError("doctor", "email", "Email can't be null"));
        var ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        var response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getValidationErrors()).containsEntry("email", "Email can't be null");
        assertThat(response.getBody().getMessage()).isEqualTo("Email can't be null");
    }
}
