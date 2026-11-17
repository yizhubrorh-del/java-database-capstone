package com.project.back_end.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.TestData;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.PatientService;

@ExtendWith(MockitoExtension.class)
class ServiceImplTest {

    @Mock private TokenServiceImpl tokenService;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private DoctorService doctorService;
    @Mock private PatientService patientService;

    @InjectMocks private ServiceImpl service;

    @Test
    void validateTokenReturnsOkForValidToken() {
        when(tokenService.validateToken("tok", "doctor")).thenReturn(true);

        var response = service.validateToken("tok", "doctor");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Token is valid");
    }

    @Test
    void validateAdminReturnsToken() {
        when(adminRepository.findByUsername("admin")).thenReturn(TestData.admin());
        when(tokenService.generateToken("admin", "admin")).thenReturn("admin-token");

        var response = service.validateAdmin("admin", "admin123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("token", "admin-token");
    }

    @Test
    void filterDoctorWithoutTimeReturnsAllMatches() {
        List<Doctor> doctors = List.of(TestData.doctor());
        when(doctorService.filterDoctors("House", "Cardiology", null, null)).thenReturn(ResponseEntity.ok(doctors));

        var response = service.filterDoctor("House", null, "Cardiology");

        assertThat(response.getBody()).containsExactlyElementsOf(doctors);
    }

    @Test
    void filterDoctorByAmKeepsDoctorsWithMorningSlots() {
        Doctor morning = TestData.doctor(); // 09:00-11:00 and 14:00-15:00
        Doctor afternoonOnly = TestData.doctor();
        afternoonOnly.setId(2L);
        afternoonOnly.setAvailableTimes(List.of("15:00-16:00"));
        when(doctorService.filterDoctors(null, null, null, null))
                .thenReturn(ResponseEntity.ok(List.of(morning, afternoonOnly)));

        var response = service.filterDoctor(null, "AM", null);

        assertThat(response.getBody()).containsExactly(morning);
    }

    @Test
    void filterDoctorByPmKeepsDoctorsWithAfternoonSlots() {
        Doctor afternoon = TestData.doctor();
        Doctor morningOnly = TestData.doctor();
        morningOnly.setId(2L);
        morningOnly.setAvailableTimes(List.of("08:00-09:00"));
        when(doctorService.filterDoctors(null, null, null, null))
                .thenReturn(ResponseEntity.ok(List.of(afternoon, morningOnly)));

        var response = service.filterDoctor(null, "pm", null);

        assertThat(response.getBody()).containsExactly(afternoon);
    }

    @Test
    void validateAppointmentReturnsOneWhenSlotIsFree() {
        AppointmentDTO dto = TestData.appointmentDTO(); // 10:00
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(TestData.doctor()));
        when(doctorService.getDoctorAvailability(1L, TestData.FUTURE_DATE))
                .thenReturn(ResponseEntity.ok(List.of("09:00-10:00", "10:00-11:00")));

        var response = service.validateAppointment(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    void validatePatientReturnsTrueForNewPatient() {
        when(patientRepository.existsByEmailOrPhone("new@mail.com", "5550000000")).thenReturn(false);

        var response = service.validatePatient("new@mail.com", "5550000000");

        assertThat(response.getBody()).isTrue();
    }

    @Test
    void validatePatientLoginReturnsToken() {
        when(patientRepository.findByEmail("john@mail.com")).thenReturn(TestData.patient());
        when(tokenService.generateToken("john@mail.com", "patient")).thenReturn("patient-token");

        var response = service.validatePatientLogin(new Login("john@mail.com", "secret123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("token", "patient-token");
    }

    @Test
    void filterPatientWithoutFiltersReturnsAllAppointments() {
        var dtos = List.of(TestData.appointmentDTO());
        when(patientService.getPatientDetails("tok")).thenReturn(ResponseEntity.ok(TestData.patient()));
        when(patientService.getPatientAppointment(10L)).thenReturn(ResponseEntity.ok(dtos));

        var response = service.filterPatient("tok", null, null);

        assertThat(response.getBody()).isEqualTo(dtos);
    }

    @Test
    void filterPatientByDoctorName() {
        var dtos = List.of(TestData.appointmentDTO());
        when(patientService.getPatientDetails("tok")).thenReturn(ResponseEntity.ok(TestData.patient()));
        when(patientService.filterByDoctor("House", 10L)).thenReturn(ResponseEntity.ok(dtos));

        var response = service.filterPatient("tok", "House", null);

        assertThat(response.getBody()).isEqualTo(dtos);
    }

    @Test
    void filterPatientByCondition() {
        var dtos = List.of(TestData.appointmentDTO());
        when(patientService.getPatientDetails("tok")).thenReturn(ResponseEntity.ok(TestData.patient()));
        when(patientService.filterByCondition(0, 10L)).thenReturn(ResponseEntity.ok(dtos));

        var response = service.filterPatient("tok", null, 0);

        assertThat(response.getBody()).isEqualTo(dtos);
    }

    @Test
    void filterPatientByDoctorNameAndCondition() {
        var dtos = List.of(TestData.appointmentDTO());
        when(patientService.getPatientDetails("tok")).thenReturn(ResponseEntity.ok(TestData.patient()));
        when(patientService.filterByDoctorAndCondition("House", 10L, 1)).thenReturn(ResponseEntity.ok(dtos));

        var response = service.filterPatient("tok", "House", 1);

        assertThat(response.getBody()).isEqualTo(dtos);
    }
}
