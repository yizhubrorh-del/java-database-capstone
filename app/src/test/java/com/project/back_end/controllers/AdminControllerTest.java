package com.project.back_end.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ServiceImpl service;
    @MockitoBean private TokenServiceImpl tokenService; // required by JwtAuthenticationFilter

    @Test
    void adminLoginReturnsToken() throws Exception {
        when(service.validateAdmin("admin", "admin123")).thenReturn(ResponseEntity.ok(Map.of("token", "admin-token")));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-token"));
    }

    @Test
    void adminLoginIsAlsoMappedUnderApiPrefix() throws Exception {
        when(service.validateAdmin("admin", "admin123")).thenReturn(ResponseEntity.ok(Map.of("token", "admin-token")));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-token"));
    }
}
