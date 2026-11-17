package com.project.back_end.controllers;

import static org.mockito.ArgumentMatchers.any;
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

import com.project.back_end.DTO.Login;
import com.project.back_end.TestData;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    private static final String TOKEN = "tok";
    private static final Map<String, String> VALID = Map.of("message", "Token is valid");

    private static final String DOCTOR_JSON = """
            {
              "id": 1,
              "name": "Dr. Gregory House",
              "speciality": "Cardiology",
              "email": "house@clinic.com",
              "password": "secret123",
              "phone": "5551234567",
              "availableTimes": ["09:00-11:00"],
              "yearsOfExperience": 10,
              "clinicAdress": "221B Baker Street",
              "rating": 4.5
            }
            """;

    @Autowired private MockMvc mockMvc;

    @MockitoBean private DoctorService doctorService;
    @MockitoBean private ServiceImpl service;
    @MockitoBean private TokenServiceImpl tokenService;

    @Test
    void getDoctorAvailabilityReturnsSlots() throws Exception {
        when(service.validateToken(TOKEN, "patient")).thenReturn(ResponseEntity.ok(VALID));
        when(doctorService.getDoctorAvailability(1L, TestData.FUTURE_DATE))
                .thenReturn(ResponseEntity.ok(List.of("09:00-10:00", "10:00-11:00")));

        mockMvc.perform(get("/doctor/availability/patient/{id}/{date}/{token}", 1, TestData.FUTURE_DATE, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availability[0]").value("09:00-10:00"))
                .andExpect(jsonPath("$.availability[1]").value("10:00-11:00"));
    }

    @Test
    void getDoctorsReturnsListWithoutPasswords() throws Exception {
        when(doctorService.getDoctors()).thenReturn(ResponseEntity.ok(List.of(TestData.doctor())));

        mockMvc.perform(get("/doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].name").value("Dr. Gregory House"))
                .andExpect(jsonPath("$.doctors[0].password").doesNotExist());
    }

    @Test
    void saveDoctorReturnsCreated() throws Exception {
        when(service.validateToken(TOKEN, "admin")).thenReturn(ResponseEntity.ok(VALID));
        when(doctorService.saveDoctor(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Doctor added to db")));

        mockMvc.perform(post("/doctor/{token}", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOCTOR_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Doctor added to db"));
    }

    @Test
    void doctorLoginReturnsToken() throws Exception {
        when(doctorService.validateDoctor(new Login("house@clinic.com", "secret123")))
                .thenReturn(ResponseEntity.ok(Map.of("token", "doctor-token")));

        mockMvc.perform(post("/doctor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "house@clinic.com", "password": "secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("doctor-token"));
    }

    @Test
    void updateDoctorReturnsOk() throws Exception {
        when(service.validateToken(TOKEN, "admin")).thenReturn(ResponseEntity.ok(VALID));
        when(doctorService.updateDoctor(any())).thenReturn(ResponseEntity.ok(Map.of("message", "Doctor updated")));

        mockMvc.perform(put("/doctor/{token}", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOCTOR_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor updated"));
    }

    @Test
    void deleteDoctorReturnsOk() throws Exception {
        when(service.validateToken(TOKEN, "admin")).thenReturn(ResponseEntity.ok(VALID));
        when(doctorService.deleteDoctor(1L))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Doctor deleted successfully")));

        mockMvc.perform(delete("/doctor/{id}/{token}", 1, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor deleted successfully"));
    }

    @Test
    void filterTreatsNullStringsAsNoFilter() throws Exception {
        when(service.filterDoctor(null, "AM", null)).thenReturn(ResponseEntity.ok(List.of(TestData.doctor())));

        mockMvc.perform(get("/doctor/filter/null/AM/null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors.length()").value(1));
    }
}
