package com.project.back_end.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import com.project.back_end.services.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller 
@RequiredArgsConstructor 
public class DashboardController {

    private final ServiceImpl service;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        var validation = service.validateToken(token, "admin");
        if (validation.getStatusCode().equals(HttpStatus.OK)) {
            return "admin/adminDashboard";
        }else{
            return "redirect:/";
        }
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        var validation = service.validateToken(token, "doctor");
        if (validation.getStatusCode().equals(HttpStatus.OK)) {
            return "doctor/doctorDashboard";
        }else{
            return "redirect:/";
        }
    }
    
    

}
