package com.project.back_end.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.back_end.TestData;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    private static final String TOKEN = "tok";
    private static final Map<String, String> VALID = Map.of("message", "Token is valid");

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AppointmentService appointmentService;
    @MockitoBean private ServiceImpl service;
    @MockitoBean private TokenServiceImpl tokenService;
    @MockitoBean private DoctorRepository doctorRepository;
    @MockitoBean private PatientRepository patientRepository;

    private static String appointmentJson() {
        return """
                {
                  "id": 100,
                  "doctorId": 1,
                  "patientId": 10,
                  "appointmentTime": "%s",
                  "status": 0,
                  "reasonForVisit": "Annual check-up",
                  "notes": "Bring previous results"
                }
                """.formatted(TestData.FUTURE_TIME);
    }

    @Test
    void getAppointmentsReturnsDoctorsAppointmentsForDate() throws Exception {
        when(service.validateToken(TOKEN, "doctor")).thenReturn(ResponseEntity.ok(VALID));
        when(tokenService.extractEmail(TOKEN)).thenReturn("house@clinic.com");
        when(doctorRepository.findByEmail("house@clinic.com")).thenReturn(TestData.doctor());
        when(appointmentService.getAppointments(1L, TestData.FUTURE_DATE, null))
                .thenReturn(ResponseEntity.ok(List.of(TestData.appointmentDTO())));

        mockMvc.perform(get("/appointments/{date}/null/{token}", TestData.FUTURE_DATE, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments.length()").value(1))
                .andExpect(jsonPath("$.appointments[0].id").value(100))
                .andExpect(jsonPath("$.appointments[0].reasonForVisit").value("Annual check-up"));
    }

    @Test
    void bookAppointmentReturnsCreated() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(appointmentService.bookAppointment(any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(1));

        mockMvc.perform(post("/appointments/{token}", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Appointment booked successfully"));
    }

    @Test
    void updateAppointmentReturnsOk() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(tokenService.extractEmail(TOKEN)).thenReturn("john@mail.com");
        when(patientRepository.findByEmail("john@mail.com")).thenReturn(TestData.patient());
        when(appointmentService.updateAppointment(any()))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Appointment updated correctly")));

        mockMvc.perform(put("/appointments/{token}", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment updated correctly"));
    }

    @Test
    void cancelAppointmentReturnsOk() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(appointmentService.cancelAppointment(100L, TOKEN))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully")));

        mockMvc.perform(delete("/appointments/{id}/{token}", 100, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment cancelled successfully"));

        verify(appointmentService).cancelAppointment(100L, TOKEN);
    }
}
