package com.project.back_end.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Doctor;

@Repository 
public interface DoctorRepository extends JpaRepository<Doctor, Long>{

   Doctor findByEmail(String email);

   @Query("SELECT d FROM Doctor d " +
          "WHERE d.name LIKE CONCAT('%', :name, '%')")
   List<Doctor> findByNameLike(@Param("name") String name);

   @Query("SELECT d FROM Doctor d "+
          "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
          "AND LOWER(d.speciality) = LOWER(:speciality)")
   List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(@Param("name") String name, @Param("speciality") String speciality);

   @Query("SELECT d FROM Doctor d " +
          "WHERE LOWER(d.speciality) = LOWER(:speciality)")
   List<Doctor> findBySpecialtyIgnoreCase(@Param("speciality")String speciality);
}