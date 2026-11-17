package com.project.back_end.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.back_end.TestData;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock private AdminRepository adminRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;

    @InjectMocks private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "jwtSecret", TestData.JWT_SECRET);
    }

    @Test
    void getSigningKeyReturnsHmacKey() {
        assertThat(tokenService.getSigningKey().getAlgorithm()).startsWith("HmacSHA");
    }

    @Test
    void generatedTokenCarriesSubjectAndLowercaseRole() {
        String token = tokenService.generateToken("john@mail.com", "PATIENT");

        assertThat(token).isNotBlank();
        assertThat(tokenService.extractEmail(token)).isEqualTo("john@mail.com");
        assertThat(tokenService.extractRole(token)).isEqualTo("patient");
    }

    @Test
    void validateTokenForAdmin() {
        String token = tokenService.generateToken("admin", "admin");
        when(adminRepository.findByUsername("admin")).thenReturn(TestData.admin());

        assertThat(tokenService.validateToken(token, "admin")).isTrue();
    }

    @Test
    void validateTokenForDoctor() {
        String token = tokenService.generateToken("house@clinic.com", "doctor");
        when(doctorRepository.findByEmail("house@clinic.com")).thenReturn(TestData.doctor());

        assertThat(tokenService.validateToken(token, "doctor")).isTrue();
    }

    @Test
    void validateTokenForPatient() {
        String token = tokenService.generateToken("john@mail.com", "patient");
        when(patientRepository.findByEmail("john@mail.com")).thenReturn(TestData.patient());

        assertThat(tokenService.validateToken(token, "patient")).isTrue();
    }
}
