package com.project.back_end.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.TestData;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock private TokenServiceImpl tokenService;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;

    @InjectMocks private PatientServiceImpl patientService;

    private final Appointment appointment = TestData.appointment(TestData.doctor(), TestData.patient());

    @Test
    void createPatientPersistsPatient() {
        Patient patient = TestData.patient();

        var response = patientService.createPatient(patient);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("John Doe");
        verify(patientRepository).saveAndFlush(patient);
    }

    @Test
    void getPatientAppointmentMapsToDtos() {
        when(appointmentRepository.findByPatientId(10L)).thenReturn(List.of(appointment));

        List<AppointmentDTO> body = patientService.getPatientAppointment(10L).getBody();

        assertThat(body).hasSize(1);
        AppointmentDTO dto = body.get(0);
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getDoctorId()).isEqualTo(1L);
        assertThat(dto.getDoctorName()).isEqualTo("Dr. Gregory House");
        assertThat(dto.getPatientId()).isEqualTo(10L);
        assertThat(dto.getPatientName()).isEqualTo("John Doe");
        assertThat(dto.getPatientEmail()).isEqualTo("john@mail.com");
        assertThat(dto.getAppointmentTime()).isEqualTo(TestData.FUTURE_TIME);
        assertThat(dto.getStatus()).isZero();
    }

    @Test
    void filterByConditionReturnsMatchingAppointments() {
        when(appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(10L, 0))
                .thenReturn(List.of(appointment));

        assertThat(patientService.filterByCondition(0, 10L).getBody()).hasSize(1);
    }

    @Test
    void filterByDoctorReturnsMatchingAppointments() {
        when(appointmentRepository.filterByDoctorNameAndPatientId("House", 10L)).thenReturn(List.of(appointment));

        assertThat(patientService.filterByDoctor("House", 10L).getBody())
                .extracting(AppointmentDTO::getDoctorName)
                .containsExactly("Dr. Gregory House");
    }

    @Test
    void filterByDoctorAndConditionReturnsMatchingAppointments() {
        when(appointmentRepository.filterByDoctorNameAndPatientIdAndStatus("House", 10L, 0))
                .thenReturn(List.of(appointment));

        assertThat(patientService.filterByDoctorAndCondition("House", 10L, 0).getBody()).hasSize(1);
    }

    @Test
    void getPatientDetailsResolvesPatientFromToken() {
        Patient patient = TestData.patient();
        when(tokenService.extractEmail("tok")).thenReturn("john@mail.com");
        when(patientRepository.findByEmail("john@mail.com")).thenReturn(patient);

        var response = patientService.getPatientDetails("tok");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(patient);
    }
}
