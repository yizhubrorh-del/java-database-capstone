
package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Admin;
import com.project.back_end.services.impl.ServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor 
@RequestMapping({"${api.path}admin", "/api/admin"})
public class AdminController {

    private final ServiceImpl service;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@Valid @RequestBody Admin admin) {
        return service.validateAdmin(admin.getUsername(), admin.getPassword());
    }

}

