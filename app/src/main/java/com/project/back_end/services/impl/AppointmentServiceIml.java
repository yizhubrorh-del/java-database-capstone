package com.project.back_end.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class AppointmentServiceIml implements AppointmentService{
    private final AppointmentRepository appointmentRepository;
    private final ServiceImpl service;
    private final TokenServiceImpl tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    
    @Override 
    public ResponseEntity<Integer> bookAppointment(AppointmentDTO appointment, String reasonForVisit, String notes) {

        LocalDateTime time = appointment.getAppointmentTime();
        boolean invalidReason = reasonForVisit == null || reasonForVisit.isBlank()
                || reasonForVisit.length() < 6 || reasonForVisit.length() > 50;
        boolean invalidNotes = notes != null && notes.length() > 100;
        boolean invalidTime = time == null || !time.isAfter(LocalDateTime.now());

        if (invalidReason || invalidNotes || invalidTime) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(0);
        }


        var validate = service.validateAppointment(appointment).getBody();
        if (validate == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(0);
        }
        if (validate == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0);
        }

        Appointment newAppointment = toAppointment(appointment, reasonForVisit, notes);

        try {
            appointmentRepository.saveAndFlush(newAppointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(1);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(0);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> updateAppointment(Long appointmentId, AppointmentDTO appointmentDTO, String reasonForVisit, String notes) {
        LocalDateTime time = appointmentDTO.getAppointmentTime();
        boolean invalidReason = reasonForVisit == null || reasonForVisit.isBlank()
                || reasonForVisit.length() < 6 || reasonForVisit.length() > 50;
        boolean invalidNotes = notes != null && notes.length() > 100;
        boolean invalidTime = time == null || !time.isAfter(LocalDateTime.now());
        boolean hasNotes = notes != null && !notes.isBlank();
        boolean hasReasonForVisit = reasonForVisit != null && !reasonForVisit.isBlank();
        

        if (invalidReason || invalidNotes || invalidTime) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid parameters"));
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        boolean slotChanged = !appointment.getDoctor().getId().equals(appointmentDTO.getDoctorId())
        || !appointment.getAppointmentTime().equals(appointmentDTO.getAppointmentTime());

        if(!appointment.getPatient().getId().equals(appointmentDTO.getPatientId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Patient doesn't match"));
        }

        if (appointment.getStatus() != 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Only scheduled appointments can be updated"));
        }

        if (hasNotes) {
            appointment.setNotes(notes);
        }

        if (hasReasonForVisit) {
            appointment.setReasonForVisit(reasonForVisit);
        }

        appointment.setDoctor(doctorRepository.findById(appointmentDTO.getDoctorId()).orElseThrow(() -> new EntityNotFoundException("Doctor not found")));

        appointment.setAppointmentTime(appointmentDTO.getAppointmentTime());

        if (slotChanged) {
            var validation = service.validateAppointment(toAppointmentDTO(appointment)).getBody();
            switch (validation) {
                case 0:
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "New appointment time isn't available"));
                case -1:
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Doctor not found"));
            }
        }
        
        try {
            appointmentRepository.saveAndFlush(appointment);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Appointment updated correctly"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Data integrity violation"));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, String patientToken) {
        boolean isTokenValid = tokenService.validateToken(patientToken, "patient");
        if (isTokenValid) {
            Patient patient = patientRepository.findByEmail(tokenService.extractEmail(patientToken));
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
            if(patient.getId().equals(appointment.getPatient().getId())){
                if(appointment.getStatus() != 0){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Can't delete a completed appointment"));
                }
                
                appointmentRepository.deleteById(appointmentId);

                return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Appointment cancelled successfully"));

            }else{
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "The appointment doesn't belong to the user"));

            }
        }else{

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));

        }
    }

    @Override
    @Transactional 
    public ResponseEntity<List<AppointmentDTO>> getAppointments(Long doctorId, LocalDate date, String patientName) {

        boolean hasPatientName = patientName != null && !patientName.isBlank();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<AppointmentDTO> appointments;

        if (hasPatientName) {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, patientName, start, end).stream().map(appointment -> toAppointmentDTO(appointment)).toList();
        }else{
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end).stream().map(appointment -> toAppointmentDTO(appointment)).toList();
        }

        return ResponseEntity.status(HttpStatus.OK).body(appointments);
    }

    @Override
    @Transactional 
    public ResponseEntity<Map<String, String>> changeStatus(Long appointmentId, Integer newStatus) {

        if (newStatus == null || newStatus < 0 || newStatus > 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid new status"));
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (appointment.getStatus() != 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Appointment already completed"));
        }

        appointmentRepository.updateStatus(newStatus, appointmentId);
        
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Status updated successfully"));
    }

    // ====================== Auxiliar Methods ==================================

    private Appointment toAppointment(AppointmentDTO appointment, String reasonForVisit, String notes){
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Patient patient = patientRepository.findById(appointment.getPatientId()).orElseThrow(() -> new EntityNotFoundException("Patient not found"));
            
        return Appointment.builder()
                          .doctor(doctor)
                          .patient(patient)
                          .reasonForVisit(reasonForVisit)
                          .appointmentTime(appointment.getAppointmentTime())
                          .status(appointment.getStatus())
                          .notes(notes)
                          .build();
    }

    private AppointmentDTO toAppointmentDTO(Appointment appointment){
        return AppointmentDTO.builder()
                             .id(appointment.getId())
                             .doctorId(appointment.getDoctor().getId())
                             .doctorName(appointment.getDoctor().getName())
                             .patientId(appointment.getPatient().getId())
                             .patientName(appointment.getPatient().getName())
                             .patientEmail(appointment.getPatient().getEmail())
                             .patientPhone(appointment.getPatient().getPhone())
                             .patientAddress(appointment.getPatient().getAddress())
                             .appointmentTime(appointment.getAppointmentTime())
                             .status(appointment.getStatus())
                             .build();
    }
    
}
