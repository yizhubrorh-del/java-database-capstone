package com.project.back_end.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.project.back_end.TestData;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private TestEntityManager em;

    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctor = TestData.doctor();
        doctor.setId(null);
        doctor = em.persist(doctor);

        patient = TestData.patient();
        patient.setId(null);
        patient = em.persist(patient);

        appointment = TestData.appointment(doctor, patient);
        appointment.setId(null);
        appointment = em.persistAndFlush(appointment);
        em.clear();
    }

    @Test
    void findByDoctorIdAndAppointmentTimeBetweenReturnsDaysAppointments() {
        var result = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(), TestData.FUTURE_DATE.atStartOfDay(), TestData.FUTURE_DATE.atTime(LocalTime.MAX));

        assertThat(result).singleElement().satisfies(a -> {
            assertThat(a.getId()).isEqualTo(appointment.getId());
            assertThat(a.getDoctor().getAvailableTimes()).hasSize(2);
        });
    }

    @Test
    void findByDoctorIdAndPatientNameIgnoresCase() {
        var result = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                doctor.getId(), "JOHN", TestData.FUTURE_DATE.atStartOfDay(), TestData.FUTURE_DATE.atTime(LocalTime.MAX));

        assertThat(result).hasSize(1);
    }

    @Test
    void findByPatientIdReturnsPatientsAppointments() {
        assertThat(appointmentRepository.findByPatientId(patient.getId())).hasSize(1);
    }

    @Test
    void findByPatientIdAndStatusOrdersByTime() {
        assertThat(appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patient.getId(), 0))
                .hasSize(1);
    }

    @Test
    void filterByDoctorNameAndPatientIdMatchesPartialName() {
        assertThat(appointmentRepository.filterByDoctorNameAndPatientId("house", patient.getId())).hasSize(1);
    }

    @Test
    void filterByDoctorNameAndPatientIdAndStatus() {
        assertThat(appointmentRepository.filterByDoctorNameAndPatientIdAndStatus("house", patient.getId(), 0))
                .hasSize(1);
    }

    @Test
    void updateStatusChangesStatus() {
        appointmentRepository.updateStatus(1, appointment.getId());
        em.clear();

        assertThat(appointmentRepository.findById(appointment.getId())).get()
                .extracting(Appointment::getStatus)
                .isEqualTo(1);
    }

    @Test
    void deleteAllByDoctorIdRemovesDoctorsAppointments() {
        appointmentRepository.deleteAllByDoctorId(doctor.getId());
        em.flush();

        assertThat(appointmentRepository.findAll()).isEmpty();
    }
}
