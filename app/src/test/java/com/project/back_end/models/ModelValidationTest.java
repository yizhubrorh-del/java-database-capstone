package com.project.back_end.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.project.back_end.DTO.Login;
import com.project.back_end.TestData;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Happy path for the entity/document models: valid instances pass bean validation
 * and the derived getters/defaults behave as expected.
 */
class ModelValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validAdminHasNoViolations() {
        assertThat(validator.validate(TestData.admin())).isEmpty();
    }

    @Test
    void validDoctorHasNoViolations() {
        assertThat(validator.validate(TestData.doctor())).isEmpty();
    }

    @Test
    void validPatientHasNoViolations() {
        assertThat(validator.validate(TestData.patient())).isEmpty();
    }

    @Test
    void validAppointmentHasNoViolations() {
        var appointment = TestData.appointment(TestData.doctor(), TestData.patient());
        assertThat(validator.validate(appointment)).isEmpty();
    }

    @Test
    void validPrescriptionHasNoViolations() {
        assertThat(validator.validate(TestData.prescription())).isEmpty();
    }

    @Test
    void validPharmacyHasNoViolations() {
        assertThat(validator.validate(new Pharmacy("Central Pharmacy", "Main St 1"))).isEmpty();
    }

    @Test
    void validLoginHasNoViolations() {
        assertThat(validator.validate(new Login("john@mail.com", "secret123"))).isEmpty();
    }

    @Test
    void appointmentDerivesDateTimeAndEndTime() {
        var appointment = TestData.appointment(TestData.doctor(), TestData.patient());

        assertThat(appointment.getAppointmentDate()).isEqualTo(TestData.FUTURE_DATE);
        assertThat(appointment.getAppointmentTimeOnly()).isEqualTo(LocalTime.of(10, 0));
        assertThat(appointment.getEndTime()).isEqualTo(TestData.FUTURE_TIME.plusHours(1));
    }

    @Test
    void doctorBuilderAppliesDefaults() {
        var doctor = Doctor.builder().name("Dr. Default").build();

        assertThat(doctor.getClinicAdress()).isEqualTo("Not specified");
        assertThat(doctor.getRating()).isEqualTo(0.0);
    }

    @Test
    void prescriptionBuilderAppliesDefaultPharmacy() {
        var prescription = Prescription.builder().medication("Aspirin").build();

        assertThat(prescription.getPharmacy().getName()).isEqualTo("Not specified");
        assertThat(prescription.getPharmacy().getLocation()).isEqualTo("Not specified");
    }
}
