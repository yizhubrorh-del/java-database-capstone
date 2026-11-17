package com.project.back_end.DTO;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.project.back_end.TestData;

import jakarta.validation.Validation;

class AppointmentDTOTest {

    @Test
    void builderDerivesDateTimeOnlyAndEndTime() {
        AppointmentDTO dto = TestData.appointmentDTO();

        assertThat(dto.getAppointmentDate()).isEqualTo(TestData.FUTURE_DATE);
        assertThat(dto.getAppointmentTimeOnly()).isEqualTo(LocalTime.of(10, 0));
        assertThat(dto.getEndTime()).isEqualTo(TestData.FUTURE_TIME.plusHours(1));
    }

    @Test
    void validDtoHasNoViolations() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(TestData.appointmentDTO())).isEmpty();
        }
    }
}
