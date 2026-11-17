package com.project.back_end.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.project.back_end.TestData;
import com.project.back_end.models.Doctor;

@DataJpaTest
class DoctorRepositoryTest {

    @Autowired private DoctorRepository doctorRepository;

    @BeforeEach
    void setUp() {
        Doctor doctor = TestData.doctor();
        doctor.setId(null);
        doctorRepository.save(doctor);
    }

    @Test
    void findByEmailReturnsDoctorWithAvailableTimes() {
        Doctor found = doctorRepository.findByEmail("house@clinic.com");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Dr. Gregory House");
        assertThat(found.getAvailableTimes()).containsExactly("09:00-11:00", "14:00-15:00");
    }

    @Test
    void findByNameLikeMatchesPartialName() {
        assertThat(doctorRepository.findByNameLike("Gregory"))
                .extracting(Doctor::getEmail)
                .containsExactly("house@clinic.com");
    }

    @Test
    void findByNameAndSpecialtyIgnoresCase() {
        assertThat(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase("house", "CARDIOLOGY"))
                .hasSize(1);
    }

    @Test
    void findBySpecialtyIgnoresCase() {
        assertThat(doctorRepository.findBySpecialtyIgnoreCase("cardiology")).hasSize(1);
    }
}
