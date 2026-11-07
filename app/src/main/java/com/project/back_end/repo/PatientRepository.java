package com.project.back_end.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Patient;

@Repository 
public interface PatientRepository extends JpaRepository<Patient, Long>{
    
    Patient findByEmail(String email);

    Optional<Patient> findByEmailOrPhone(String email, String phone);
    
    boolean existsByEmailOrPhone(String email, String phone);

}

