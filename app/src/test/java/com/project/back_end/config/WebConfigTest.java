package com.project.back_end.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.back_end.controllers.AdminController;
import com.project.back_end.services.impl.ServiceImpl;
import com.project.back_end.services.impl.TokenServiceImpl;

@WebMvcTest(AdminController.class)
class WebConfigTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ServiceImpl service;
    @MockitoBean private TokenServiceImpl tokenService;

    @Test
    void corsPreflightIsAllowedForAnyOrigin() throws Exception {
        mockMvc.perform(options("/admin/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
    }
}
