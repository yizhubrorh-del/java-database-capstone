package com.project.back_end;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.models.Pharmacy;
import com.project.back_end.models.Prescription;

/**
 * Shared, valid fixtures for the happy-path test suite.
 */
public final class TestData {

    public static final String JWT_SECRET =
            "test-secret-key-that-is-long-enough-for-hs512-signing-0123456789abcdef";

    /** A fixed date a week from now, at 10:00, so it always passes @Future. */
    public static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
    public static final LocalDateTime FUTURE_TIME = FUTURE_DATE.atTime(10, 0);

    private TestData() {
    }

    public static Admin admin() {
        return Admin.builder().id(1L).username("admin").password("admin123").build();
    }

    public static Doctor doctor() {
        return Doctor.builder()
                .id(1L)
                .name("Dr. Gregory House")
                .speciality("Cardiology")
                .email("house@clinic.com")
                .password("secret123")
                .phone("5551234567")
                .availableTimes(new ArrayList<>(List.of("09:00-11:00", "14:00-15:00")))
                .yearsOfExperience(10)
                .clinicAdress("221B Baker Street")
                .rating(4.5)
                .build();
    }

    public static Patient patient() {
        return Patient.builder()
                .id(10L)
                .name("John Doe")
                .email("john@mail.com")
                .password("secret123")
                .phone("5559876543")
                .address("742 Evergreen Terrace")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .emergencyContact("5550001111")
                .insuranceProvider("HealthPlus")
                .build();
    }

    public static Appointment appointment(Doctor doctor, Patient patient) {
        return Appointment.builder()
                .id(100L)
                .doctor(doctor)
                .patient(patient)
                .reasonForVisit("Annual check-up")
                .appointmentTime(FUTURE_TIME)
                .status(0)
                .notes("Bring previous results")
                .build();
    }

    public static AppointmentDTO appointmentDTO() {
        return AppointmentDTO.builder()
                .id(100L)
                .doctorId(1L)
                .patientId(10L)
                .appointmentTime(FUTURE_TIME)
                .status(0)
                .reasonForVisit("Annual check-up")
                .notes("Bring previous results")
                .build();
    }

    public static Prescription prescription() {
        return Prescription.builder()
                .id("rx-1")
                .patientName("John Doe")
                .appointmentId(100L)
                .medication("Ibuprofen")
                .dosage("400mg every 8h")
                .doctorNotes("Take after meals")
                .refillCount(2)
                .pharmacy(new Pharmacy("Central Pharmacy", "Main St 1"))
                .build();
    }
}
