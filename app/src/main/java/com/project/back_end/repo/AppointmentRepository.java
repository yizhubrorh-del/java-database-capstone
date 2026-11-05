package com.project.back_end.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Citas de un doctor en un rango de tiempo, cargando sus horarios disponibles (evita lazy loading).
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH d.availableTimes " +
           "WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Citas de un doctor filtradas por nombre de paciente (sin distinguir mayúsculas) en un rango de tiempo.
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH a.patient p " +
           "WHERE d.id = :doctorId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Elimina todas las citas de un doctor 
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    // Todas las citas de un paciente 
    List<Appointment> findByPatientId(Long patientId);

    // Citas de un paciente con un estado dado, ordenadas por fecha 
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    // Citas filtradas por nombre de doctor (LIKE) y paciente.
    @Query("SELECT a FROM Appointment a " +
           "WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId);

    // Citas filtradas por nombre de doctor (LIKE), paciente y estado.
    @Query("SELECT a FROM Appointment a " +
           "WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId " +
           "AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);

    // Actualiza el estado de una cita.
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);
}
