package com.project.back_end.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.TestData;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImlTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ServiceImpl service;
    @Mock private TokenServiceImpl tokenService;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks private AppointmentServiceIml appointmentService;

    private final Doctor doctor = TestData.doctor();
    private final Patient patient = TestData.patient();

    @Test
    void bookAppointmentSavesWhenSlotIsFree() {
        AppointmentDTO dto = TestData.appointmentDTO();
        when(service.validateAppointment(dto)).thenReturn(ResponseEntity.ok(1));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));

        var response = appointmentService.bookAppointment(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(1);

        ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getDoctor()).isEqualTo(doctor);
        assertThat(saved.getValue().getPatient()).isEqualTo(patient);
        assertThat(saved.getValue().getAppointmentTime()).isEqualTo(TestData.FUTURE_TIME);
        assertThat(saved.getValue().getReasonForVisit()).isEqualTo("Annual check-up");
    }

    @Test
    void updateAppointmentWithSameSlotUpdatesDetails() {
        Appointment existing = TestData.appointment(doctor, patient);
        AppointmentDTO dto = TestData.appointmentDTO();
        dto.setReasonForVisit("Follow-up visit");
        dto.setNotes("Updated notes");
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        var response = appointmentService.updateAppointment(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Appointment updated correctly");
        assertThat(existing.getReasonForVisit()).isEqualTo("Follow-up visit");
        assertThat(existing.getNotes()).isEqualTo("Updated notes");
        verify(service, never()).validateAppointment(any());
        verify(appointmentRepository).saveAndFlush(existing);
    }

    @Test
    void updateAppointmentToNewFreeSlotRevalidatesAndSaves() {
        Appointment existing = TestData.appointment(doctor, patient);
        AppointmentDTO dto = TestData.appointmentDTO();
        dto.setAppointmentTime(TestData.FUTURE_DATE.atTime(LocalTime.of(14, 0)));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(service.validateAppointment(any())).thenReturn(ResponseEntity.ok(1));

        var response = appointmentService.updateAppointment(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existing.getAppointmentTime()).isEqualTo(TestData.FUTURE_DATE.atTime(14, 0));
        verify(appointmentRepository).saveAndFlush(existing);
    }

    @Test
    void cancelAppointmentDeletesOwnScheduledAppointment() {
        Appointment existing = TestData.appointment(doctor, patient);
        when(tokenService.validateToken("tok", "patient")).thenReturn(true);
        when(tokenService.extractEmail("tok")).thenReturn("john@mail.com");
        when(patientRepository.findByEmail("john@mail.com")).thenReturn(patient);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existing));

        var response = appointmentService.cancelAppointment(100L, "tok");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Appointment cancelled successfully");
        verify(appointmentRepository).deleteById(100L);
    }

    @Test
    void getAppointmentsWithoutPatientNameReturnsDoctorsDay() {
        when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                1L, TestData.FUTURE_DATE.atStartOfDay(), TestData.FUTURE_DATE.atTime(LocalTime.MAX)))
                .thenReturn(List.of(TestData.appointment(doctor, patient)));

        var response = appointmentService.getAppointments(1L, TestData.FUTURE_DATE, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(100L);
            assertThat(dto.getPatientName()).isEqualTo("John Doe");
            assertThat(dto.getReasonForVisit()).isEqualTo("Annual check-up");
        });
    }

    @Test
    void getAppointmentsWithPatientNameFiltersByName() {
        when(appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                1L, "john", TestData.FUTURE_DATE.atStartOfDay(), TestData.FUTURE_DATE.atTime(LocalTime.MAX)))
                .thenReturn(List.of(TestData.appointment(doctor, patient)));

        var response = appointmentService.getAppointments(1L, TestData.FUTURE_DATE, "john");

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void changeStatusUpdatesScheduledAppointment() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(TestData.appointment(doctor, patient)));

        var response = appointmentService.changeStatus(100L, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Status updated successfully");
        verify(appointmentRepository).updateStatus(1, 100L);
    }
}
