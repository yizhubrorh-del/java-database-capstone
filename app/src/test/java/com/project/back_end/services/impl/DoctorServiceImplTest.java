package com.project.back_end.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.project.back_end.DTO.Login;
import com.project.back_end.TestData;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

/**
 * Note: unstubbed appointmentRepository list queries return an empty list (Mockito default),
 * which means "no booked appointments" - every configured slot is free.
 */
@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private TokenServiceImpl tokenService;

    @InjectMocks private DoctorServiceImpl doctorService;

    @Test
    void getDoctorAvailabilitySplitsRangesIntoHourlySlots() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(TestData.doctor()));

        var response = doctorService.getDoctorAvailability(1L, TestData.FUTURE_DATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("09:00-10:00", "10:00-11:00", "14:00-15:00");
    }

    @Test
    void getDoctorAvailabilityExcludesBookedSlots() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(eq(1L), any(), any()))
                .thenReturn(List.of(TestData.appointment(doctor, TestData.patient()))); // 10:00-11:00

        var response = doctorService.getDoctorAvailability(1L, TestData.FUTURE_DATE);

        assertThat(response.getBody()).containsExactly("09:00-10:00", "14:00-15:00");
    }

    @Test
    void saveDoctorCreatesNewDoctor() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findByEmail(doctor.getEmail())).thenReturn(null);

        var response = doctorService.saveDoctor(doctor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("message", "Doctor added to db");
        assertThat(doctor.getId()).isNull();
        verify(doctorRepository).saveAndFlush(doctor);
    }

    @Test
    void updateDoctorSavesExistingDoctor() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.existsById(1L)).thenReturn(true);

        var response = doctorService.updateDoctor(doctor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Doctor updated");
        verify(doctorRepository).saveAndFlush(doctor);
    }

    @Test
    void getDoctorsReturnsAll() {
        List<Doctor> doctors = List.of(TestData.doctor());
        when(doctorRepository.findAll()).thenReturn(doctors);

        assertThat(doctorService.getDoctors().getBody()).isEqualTo(doctors);
    }

    @Test
    void deleteDoctorRemovesAppointmentsThenDoctor() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        var response = doctorService.deleteDoctor(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var order = inOrder(appointmentRepository, doctorRepository);
        order.verify(appointmentRepository).deleteAllByDoctorId(1L);
        order.verify(doctorRepository).deleteById(1L);
        order.verify(doctorRepository).flush();
    }

    @Test
    void validateDoctorReturnsToken() {
        when(doctorRepository.findByEmail("house@clinic.com")).thenReturn(TestData.doctor());
        when(tokenService.generateToken("house@clinic.com", "doctor")).thenReturn("doctor-token");

        var response = doctorService.validateDoctor(new Login("house@clinic.com", "secret123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("token", "doctor-token");
    }

    @Test
    void findDoctorByNameDelegatesToRepository() {
        List<Doctor> doctors = List.of(TestData.doctor());
        when(doctorRepository.findByNameLike("House")).thenReturn(doctors);

        assertThat(doctorService.findDoctorByName("House").getBody()).isEqualTo(doctors);
    }

    @Test
    void filterDoctorsByNameSpecialtyAndTimeKeepsAvailableDoctors() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase("House", "Cardiology"))
                .thenReturn(List.of(doctor));

        var response = doctorService.filterDoctorsByNameSpecialtyAndTime(
                "House", "Cardiology", TestData.FUTURE_DATE.atTime(9, 0));

        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void filterDoctorByTimeKeepsDoctorsWithSlotInRange() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        var response = doctorService.filterDoctorByTime(
                TestData.FUTURE_DATE.atTime(8, 0), TestData.FUTURE_DATE.atTime(12, 0));

        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void filterDoctorByNameAndTimeKeepsAvailableDoctors() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findByNameLike("House")).thenReturn(List.of(doctor));

        var response = doctorService.filterDoctorByNameAndTime("House", TestData.FUTURE_DATE.atTime(14, 0));

        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void filterDoctorByNameAndSpecialityDelegatesToRepository() {
        List<Doctor> doctors = List.of(TestData.doctor());
        when(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase("House", "Cardiology"))
                .thenReturn(doctors);

        assertThat(doctorService.filterDoctorByNameAndSpeciality("House", "Cardiology").getBody()).isEqualTo(doctors);
    }

    @Test
    void filterDoctorByTimeAndSpecialityKeepsAvailableDoctors() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findBySpecialtyIgnoreCase("Cardiology")).thenReturn(List.of(doctor));

        var response = doctorService.filterDoctorByTimeAndSpeciality(TestData.FUTURE_DATE.atTime(10, 0), "Cardiology");

        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void filterDoctorBySpecialityDelegatesToRepository() {
        List<Doctor> doctors = List.of(TestData.doctor());
        when(doctorRepository.findBySpecialtyIgnoreCase("Cardiology")).thenReturn(doctors);

        assertThat(doctorService.filterDoctorBySpeciality("Cardiology").getBody()).isEqualTo(doctors);
    }

    @Test
    void filterDoctorsWithoutDatesUsesNameAndSpecialty() {
        List<Doctor> doctors = List.of(TestData.doctor());
        when(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase("House", "Cardiology"))
                .thenReturn(doctors);

        assertThat(doctorService.filterDoctors("House", "Cardiology", null, null).getBody()).isEqualTo(doctors);
    }

    @Test
    void filterDoctorsWithDateRangeKeepsAvailableDoctors() {
        Doctor doctor = TestData.doctor();
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        var response = doctorService.filterDoctors(null, null, TestData.FUTURE_DATE, TestData.FUTURE_DATE.plusDays(1));

        assertThat(response.getBody()).containsExactly(doctor);
    }
}
