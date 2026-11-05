package com.project.back_end.services.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.services.DoctorService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DoctorServiceImpl implements DoctorService{
    
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenServiceImpl tokenService;

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final Duration APPOINTMENT_DURATION = Duration.ofHours(1);
    
    @Override
    @Transactional
    public ResponseEntity<List<String>> getDoctorAvailability(Long doctorId, LocalDate date) {

        Doctor doctor = doctorRepository.findById(doctorId)
                                        .orElseThrow(() -> new EntityNotFoundException("Doctor not found with the id: " + doctorId));

        return ResponseEntity.ok(getAvailableTimes(doctor, date));
    }

    @Override
    @Transactional 
    public ResponseEntity<String> saveDoctor(Doctor doctor) {
        doctorRepository.save(doctor);
        return ResponseEntity.ok("Doctor " + doctor.getName() + " added correctly.");
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> getDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @Override
    @Transactional 
    public ResponseEntity<String> deleteDoctor(Long doctorId) {
        if(!doctorRepository.existsById(doctorId)){
            throw new EntityNotFoundException("Doctor with id " + doctorId + " not found.");
        }

        appointmentRepository.deleteAllByDoctorId(doctorId);
        doctorRepository.deleteById(doctorId);
        return ResponseEntity.ok("Doctor with id " + doctorId + " deleted correctly.");
    }

    @Override
    @Transactional 
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());

        if(doctor == null || !doctor.getPassword().equals(login.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }

        String token = tokenService.generateToken(doctor.getEmail(), "doctor");

        return ResponseEntity.ok(Map.of("token", token));
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> findDoctorByName(String name) {
        return ResponseEntity.ok(doctorRepository.findByNameLike(name));
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> filterDoctorsByNameSpecialtyAndTime(String name, String speciality,
            LocalDateTime availableTime) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, speciality)
                                               .stream()
                                               .filter(doctor -> isAvailableAt(doctor, availableTime))
                                               .toList();
        
        return ResponseEntity.ok(doctors);
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> filterDoctorByTime(LocalDateTime start, LocalDateTime end) {
       if(start == null || end == null || !start.isBefore((end))){
        throw new IllegalArgumentException("Start must be before end");
       }

       List<Doctor> doctors = doctorRepository.findAll().stream()
                            .filter(doctor -> hasAvailabilityBetween(doctor, start, end))
                            .toList();

        return ResponseEntity.ok(doctors);
    }

    @Override
    @Transactional
    public ResponseEntity<List<Doctor>> filterDoctorByNameAndTime(String name, LocalDateTime availableTime) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name).stream()
                            .filter(doctor -> isAvailableAt(doctor, availableTime))
                            .toList();
        return ResponseEntity.ok(doctors);
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> filterDoctorByNameAndSpeciality(String name, String speciality) {
        return ResponseEntity.ok(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, speciality));
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> filterDoctorByTimeAndSpeciality(LocalDateTime availableTime,
            String speciality) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(speciality).stream()
                            .filter(doctor -> isAvailableAt(doctor, availableTime))
                            .toList();

        return ResponseEntity.ok(doctors);
    }

    @Override
    @Transactional 
    public ResponseEntity<List<Doctor>> filterDoctorBySpeciality(String speciality) {
        return ResponseEntity.ok(doctorRepository.findBySpecialtyIgnoreCase(speciality));
    }

    // ===================== Métodos auxiliares de disponibilidad =====================

    /**
     * Devuelve los huecos libres del doctor en una fecha concreta.
     * Las franjas amplias (p. ej. "09:00-13:00") se dividen en huecos de APPOINTMENT_DURATION
     * y se descartan los que se solapan con alguna cita ya agendada.
     */
    private List<String> getAvailableTimes(Doctor doctor, LocalDate date) {
        if (doctor.getAvailableTimes() == null || doctor.getAvailableTimes().isEmpty()) {
            return List.of();
        }

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(), date.atStartOfDay(), date.atTime(LocalTime.MAX));

        return doctor.getAvailableTimes().stream()
                .map(TimeSlot::parse)
                .flatMap(this::split)
                .filter(slot -> appointments.stream().noneMatch(a ->
                        slot.overlaps(a.getAppointmentTime().toLocalTime(),
                                      a.getEndTime().toLocalTime())))
                .map(TimeSlot::toString)
                .toList();
    }

    /**
     * Indica si una cita que empieza en dateTime cabe completa en algún hueco libre del doctor.
     */
    private boolean isAvailableAt(Doctor doctor, LocalDateTime dateTime) {
        LocalTime start = dateTime.toLocalTime();
        LocalTime end = start.plus(APPOINTMENT_DURATION);

        return getAvailableTimes(doctor, dateTime.toLocalDate()).stream()
                .map(TimeSlot::parse)
                .anyMatch(slot -> !start.isBefore(slot.start()) && !end.isAfter(slot.end()));
    }

    /**
     * Divide una franja en huecos consecutivos de APPOINTMENT_DURATION.
     * "09:00-11:00" -> [09:00-10:00, 10:00-11:00]. Una franja de 1 h se devuelve tal cual.
     */
    private Stream<TimeSlot> split(TimeSlot range) {
        return Stream.iterate(range.start(),
                        t -> !t.plus(APPOINTMENT_DURATION).isAfter(range.end()),
                        t -> t.plus(APPOINTMENT_DURATION))
                .map(t -> new TimeSlot(t, t.plus(APPOINTMENT_DURATION)));
    }

    /**
     * Indica si el doctor tiene al menos un hueco libre contenido por completo en [start, end]
     * Recorre cada día del rango y se detiene en cuanto encuentra uno.
     * 
     */ 
    private boolean hasAvailabilityBetween(Doctor doctor, LocalDateTime start, LocalDateTime end){
        return start.toLocalDate()
                    .datesUntil(end.toLocalDate().plusDays(1))
                    .anyMatch(date -> getAvailableTimes(doctor, date).stream()
                    .map(TimeSlot::parse)
                    .anyMatch(slot -> {
                        LocalDateTime slotStart = date.atTime(slot.start());
                        LocalDateTime slotEnd = date.atTime(slot.end());
                        return !slotStart.isBefore(start) && !slotEnd.isAfter(end);
                    }));
    }


    /**
     * Intervalo horario [start, end). Se construye a partir de strings con formato "HH:mm-HH:mm".
     */
    private record TimeSlot(LocalTime start, LocalTime end) {

        static TimeSlot parse(String slot) {
            String[] parts = slot.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid time slot format (expected HH:mm-HH:mm): " + slot);
            }
            return new TimeSlot(LocalTime.parse(parts[0].trim(), HH_MM),
                                LocalTime.parse(parts[1].trim(), HH_MM));
        }

        boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
            return start.isBefore(otherEnd) && otherStart.isBefore(end);
        }

        @Override
        public String toString() {
            return start.format(HH_MM) + "-" + end.format(HH_MM);
        }
    }
}
