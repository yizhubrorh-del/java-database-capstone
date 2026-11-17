package com.project.back_end.mvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ServiceImpl service;
    @MockitoBean private TokenServiceImpl tokenService;

    @Test
    void adminDashboardRendersForValidAdminToken() throws Exception {
        when(service.validateToken("admin-token", "admin")).thenReturn(ResponseEntity.ok(Map.of("message", "ok")));

        mockMvc.perform(get("/adminDashboard/{token}", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminDashboard"));
    }

    @Test
    void doctorDashboardRendersForValidDoctorToken() throws Exception {
        when(service.validateToken("doctor-token", "doctor")).thenReturn(ResponseEntity.ok(Map.of("message", "ok")));

        mockMvc.perform(get("/doctorDashboard/{token}", "doctor-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctorDashboard"));
    }
}
