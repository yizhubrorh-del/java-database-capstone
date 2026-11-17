package com.project.back_end.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.project.back_end.TestData;
import com.project.back_end.models.Patient;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        Patient patient = TestData.patient();
        patient.setId(null);
        patientRepository.save(patient);
    }

    @Test
    void findByEmailReturnsPatient() {
        assertThat(patientRepository.findByEmail("john@mail.com").getName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmailOrPhoneMatchesByPhone() {
        assertThat(patientRepository.findByEmailOrPhone("other@mail.com", "5559876543"))
                .get()
                .extracting(Patient::getEmail)
                .isEqualTo("john@mail.com");
    }

    @Test
    void existsByEmailOrPhoneMatchesByEmail() {
        assertThat(patientRepository.existsByEmailOrPhone("john@mail.com", "0000000000")).isTrue();
    }
}
