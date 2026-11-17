package com.project.back_end.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.project.back_end.TestData;
import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock private PrescriptionRepository prescriptionRepository;

    @InjectMocks private PrescriptionServiceImpl prescriptionService;

    @Test
    void savePrescriptionCreatesNewPrescription() {
        Prescription prescription = TestData.prescription();
        when(prescriptionRepository.existsByAppointmentId(100L)).thenReturn(false);

        var response = prescriptionService.savePrescription(prescription);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("message", "Prescription created");
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    void getPrescriptionReturnsItWrappedInList() {
        Prescription prescription = TestData.prescription();
        when(prescriptionRepository.findByAppointmentId(100L)).thenReturn(Optional.of(prescription));

        var response = prescriptionService.getPrescription(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("prescription", List.of(prescription)));
    }
}
