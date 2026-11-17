package com.project.back_end.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.back_end.DTO.Login;
import com.project.back_end.TestData;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    private static final String TOKEN = "tok";
    private static final Map<String, String> VALID = Map.of("message", "Token is valid");

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PatientService patientService;
    @MockitoBean private ServiceImpl service;
    @MockitoBean private TokenServiceImpl tokenService;

    @Test
    void getPatientReturnsDetailsWithoutPassword() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(patientService.getPatientDetails(TOKEN)).thenReturn(ResponseEntity.ok(TestData.patient()));

        mockMvc.perform(get("/patient/{token}", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.name").value("John Doe"))
                .andExpect(jsonPath("$.patient.email").value("john@mail.com"))
                .andExpect(jsonPath("$.patient.password").doesNotExist());
    }

    @Test
    void createPatientReturnsCreated() throws Exception {
        when(service.validatePatient("jane@mail.com", "5551112222")).thenReturn(ResponseEntity.ok(true));
        when(patientService.createPatient(any())).thenReturn(ResponseEntity.ok("created"));

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Roe",
                                  "email": "jane@mail.com",
                                  "password": "secret123",
                                  "phone": "5551112222",
                                  "address": "12 Elm Street",
                                  "dateOfBirth": "1992-05-20",
                                  "emergencyContact": "5553334444",
                                  "insuranceProvider": "HealthPlus"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Signup successful"));

        verify(patientService).createPatient(any());
    }

    @Test
    void loginReturnsToken() throws Exception {
        when(service.validatePatientLogin(new Login("john@mail.com", "secret123")))
                .thenReturn(ResponseEntity.ok(Map.of("token", "patient-token")));

        mockMvc.perform(post("/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@mail.com", "password": "secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("patient-token"));
    }

    @Test
    void patientCanSeeOwnAppointments() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(patientService.getPatientDetails(TOKEN)).thenReturn(ResponseEntity.ok(TestData.patient()));
        when(patientService.getPatientAppointment(10L))
                .thenReturn(ResponseEntity.ok(List.of(TestData.appointmentDTO())));

        mockMvc.perform(get("/patient/{id}/patient/{token}", 10, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments.length()").value(1));
    }

    @Test
    void doctorCanSeePatientAppointments() throws Exception {
        when(service.validateToken(TOKEN, "doctor")).thenReturn(ResponseEntity.ok(VALID));
        when(patientService.getPatientAppointment(10L))
                .thenReturn(ResponseEntity.ok(List.of(TestData.appointmentDTO())));

        mockMvc.perform(get("/patient/{id}/doctor/{token}", 10, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments[0].patientId").value(10));
    }

    @Test
    void filterMapsConditionAndNullName() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(service.filterPatient(TOKEN, null, 0)).thenReturn(ResponseEntity.ok(List.of(TestData.appointmentDTO())));

        mockMvc.perform(get("/patient/filter/future/null/{token}", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments.length()").value(1));
    }

    @Test
    void filterByDoctorNameAndPastCondition() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(service.filterPatient(TOKEN, "House", 1)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/patient/filter/past/House/{token}", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments.length()").value(0));
    }
}
