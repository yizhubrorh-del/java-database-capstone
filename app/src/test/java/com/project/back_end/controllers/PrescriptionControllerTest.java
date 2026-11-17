package com.project.back_end.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.back_end.TestData;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(PrescriptionController.class)
class PrescriptionControllerTest {

    private static final String TOKEN = "tok";
    private static final Map<String, String> VALID = Map.of("message", "Token is valid");

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PrescriptionService prescriptionService;
    @MockitoBean private ServiceImpl service;
    @MockitoBean private AppointmentService appointmentService;
    @MockitoBean private TokenServiceImpl tokenService;

    @Test
    void savePrescriptionCreatesItAndCompletesAppointment() throws Exception {
        when(service.validateToken(TOKEN, "doctor")).thenReturn(ResponseEntity.ok(VALID));
        when(prescriptionService.savePrescription(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Prescription created")));

        mockMvc.perform(post("/prescription/{token}", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientName": "John Doe",
                                  "appointmentId": 100,
                                  "medication": "Ibuprofen",
                                  "dosage": "400mg every 8h",
                                  "doctorNotes": "Take after meals",
                                  "refillCount": 2,
                                  "pharmacy": {"name": "Central Pharmacy", "location": "Main St 1"}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Prescription created"));

        verify(appointmentService).changeStatus(100L, 1);
    }

    @Test
    void getPrescriptionReturnsIt() throws Exception {
        when(service.validateToken(TOKEN, "doctor")).thenReturn(ResponseEntity.ok(VALID));
        doReturn(ResponseEntity.ok(Map.of("prescription", List.of(TestData.prescription()))))
                .when(prescriptionService).getPrescription(100L);

        mockMvc.perform(get("/prescription/{appointmentId}/{token}", 100, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prescription[0].medication").value("Ibuprofen"))
                .andExpect(jsonPath("$.prescription[0].pharmacy.name").value("Central Pharmacy"));
    }
}
