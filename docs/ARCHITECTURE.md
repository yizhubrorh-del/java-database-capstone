# Architecture

This document describes how the Smart Clinic Management System is built, how a request moves through it,
and where the design is strong or needs work. For endpoint details see [API.md](API.md). For column-level
schema see [schema-design.md](../schema-design.md).

## Contents

1. [System overview](#1-system-overview)
2. [Layered architecture](#2-layered-architecture)
3. [Domain model](#3-domain-model)
4. [Class diagrams](#4-class-diagrams)
5. [Key request flows](#5-key-request-flows)
6. [Security model](#6-security-model)
7. [Persistence](#7-persistence)
8. [Error handling](#8-error-handling)
9. [Frontend](#9-frontend)
10. [Deployment and CI](#10-deployment-and-ci)

---

## 1. System overview

The system is a **monolithic Spring Boot application** that follows a classic **three-tier architecture**. One
deployable JAR serves the static frontend, renders two server-side pages and exposes a REST API. Data is split
across two databases by its shape:

- **MySQL** holds the relational core: admins, doctors, patients and appointments, where foreign keys and
  uniqueness matter.
- **MongoDB** holds prescriptions: semi-structured documents with an embedded pharmacy that are only read
  per appointment.

```mermaid
flowchart LR
    subgraph Client["Browser"]
        SPA["Static pages + JS modules<br/>(index, patient pages)"]
        TH["Thymeleaf dashboards<br/>(admin, doctor)"]
    end

    subgraph App["Spring Boot application (port 8080)"]
        MVC["MVC controller<br/>DashboardController"]
        REST["REST controllers<br/>Admin / Doctor / Patient /<br/>Appointment / Prescription"]
        SVC["Service layer<br/>(+ ServiceImpl, TokenServiceImpl)"]
        JPA["Spring Data JPA repositories"]
        MREPO["Spring Data MongoDB repository"]
    end

    MySQL[("MySQL<br/>cms")]
    Mongo[("MongoDB<br/>cms.prescriptions")]

    SPA -- "fetch() JSON" --> REST
    TH -- "GET /xDashboard/{token}" --> MVC
    TH -- "fetch() JSON" --> REST
    MVC --> SVC
    REST --> SVC
    SVC --> JPA --> MySQL
    SVC --> MREPO --> Mongo
```

---

## 2. Layered architecture

| Tier | Package / location | Responsibility |
|---|---|---|
| **Presentation** | `resources/static`, `resources/templates` | HTML/CSS/JS. Calls the API with `fetch`. Stores the JWT and role in `localStorage`. |
| **Web** | `mvc`, `controllers` | Map HTTP to Java: parse path variables and bodies, run Bean Validation (`@Valid`), validate the token's role, shape the JSON (`{"message"}`, `{"doctors": [...]}`...). |
| **Service** | `services`, `services.impl` | Business rules: slot availability, ownership checks, status transitions, duplicate detection, filtering. |
| **Persistence** | `repo`, `models` | Spring Data repositories (derived queries and JPQL `@Query`), JPA entities and Mongo documents. |
| **Cross-cutting** | `Exception`, `config`, `DTO` | Exception handling, JWT filter, CORS, transfer objects. |

```mermaid
flowchart TB
    subgraph web["Web layer"]
        DC[DashboardController]
        AC[AdminController]
        DOC[DoctorController]
        PC[PatientController]
        APC[AppointmentController]
        PRC[PrescriptionController]
    end

    subgraph service["Service layer"]
        S[ServiceImpl<br/><i>shared facade</i>]
        TS[TokenServiceImpl]
        DS[DoctorService]
        PS[PatientService]
        AS[AppointmentService]
        PRS[PrescriptionService]
    end

    subgraph data["Persistence layer"]
        AR[(AdminRepository)]
        DR[(DoctorRepository)]
        PR[(PatientRepository)]
        APR[(AppointmentRepository)]
        PRR[(PrescriptionRepository)]
    end

    DC --> S
    AC --> S
    DOC --> S & DS
    PC --> S & PS
    APC --> S & AS & TS
    APC -. direct .-> DR & PR
    PRC --> S & PRS & AS

    S --> TS & DS & PS
    S --> AR & DR & PR
    TS --> AR & DR & PR
    DS --> DR & APR & TS
    PS --> PR & APR & TS
    AS --> APR & DR & PR & TS & S
    PRS --> PRR
```

The dotted arrows mark the places where a controller skips the service layer (see
[§11](#11-architecture-assessment)).

### The `ServiceImpl` facade

`ServiceImpl` collects logic that more than one controller needs:

| Method | Used by | Purpose |
|---|---|---|
| `validateToken(token, role)` | every protected endpoint | Returns `200` or `401 {"message"}` so controllers can return it as is |
| `validateAdmin(username, pwd)` | AdminController | Admin login, issues a JWT |
| `validatePatientLogin(login)` | PatientController | Patient login, issues a JWT |
| `validatePatient(email, phone)` | PatientController | `true` if neither the email nor the phone is registered |
| `validateAppointment(dto)` | AppointmentService | `1` slot free, `0` slot taken, `-1` unknown doctor |
| `filterDoctor(name, time, speciality)` | DoctorController | Name/speciality filter plus AM/PM availability filter |
| `filterPatient(token, doctorName, condition)` | PatientController | Resolves the patient from the token, then picks the right filter |

---

## 3. Domain model

```mermaid
erDiagram
    ADMIN {
        BIGINT id PK
        VARCHAR username
        VARCHAR password
    }
    DOCTOR {
        BIGINT id PK
        VARCHAR name
        VARCHAR speciality
        VARCHAR email
        VARCHAR password
        VARCHAR phone
        INT years_of_experience
        VARCHAR clinic_adress
        DOUBLE rating
    }
    DOCTOR_AVAILABLE_TIMES {
        BIGINT doctor_id FK
        VARCHAR available_times "e.g. 09:00-13:00"
    }
    PATIENT {
        BIGINT id PK
        VARCHAR name
        VARCHAR email
        VARCHAR password
        VARCHAR phone
        VARCHAR address
        DATE date_of_birth
        VARCHAR emergency_contact
        VARCHAR insurance_provider
    }
    APPOINTMENT {
        BIGINT id PK
        BIGINT doctor_id FK
        BIGINT patient_id FK
        DATETIME appointment_time "unique with doctor_id"
        INT status "0 scheduled, 1 completed, 2 cancelled"
        VARCHAR reason_for_visit
        VARCHAR notes
    }
    PRESCRIPTION {
        STRING id PK "MongoDB ObjectId"
        BIGINT appointmentId "logical ref to APPOINTMENT"
        STRING patientName
        STRING medication
        STRING dosage
        STRING doctorNotes
        INT refillCount
        OBJECT pharmacy "embedded {name, location}"
    }

    DOCTOR ||--o{ DOCTOR_AVAILABLE_TIMES : "works in"
    DOCTOR ||--o{ APPOINTMENT : attends
    PATIENT ||--o{ APPOINTMENT : books
    APPOINTMENT ||--o| PRESCRIPTION : "results in (cross-database)"
```

Key rules that live in the model:

- **One appointment per doctor and time slot.** This is enforced by the unique constraint
  `uk_appointment_doctor_time (doctor_id, appointment_time)`. It is the last line of defence against two patients
  booking the same slot at the same time.
- **Appointments last one hour.** `endTime`, `appointmentDate` and `appointmentTimeOnly` are derived `@Transient`
  getters and are not stored.
- **Availability is a set of ranges.** `availableTimes` stores ranges like `09:00-13:00`. The service splits them
  into 1-hour slots and removes the ones already booked.
- **Prescription to appointment is a logical link.** `appointmentId` is a plain number in MongoDB, so no
  database enforces it.

---

## 4. Class diagrams

### 4.1 Domain model and DTOs

```mermaid
classDiagram
    direction LR

    class Admin {
        -Long id
        -String username
        -String password
    }

    class Doctor {
        -Long id
        -String name
        -String speciality
        -String email
        -String password
        -String phone
        -List~String~ availableTimes
        -int yearsOfExperience
        -String clinicAdress = "Not specified"
        -Double rating = 0.0
    }

    class Patient {
        -Long id
        -String name
        -String email
        -String password
        -String phone
        -String address
        -LocalDate dateOfBirth
        -String emergencyContact
        -String insuranceProvider
    }

    class Appointment {
        -Long id
        -Doctor doctor
        -Patient patient
        -String reasonForVisit
        -LocalDateTime appointmentTime
        -int status
        -String notes
        +getEndTime() LocalDateTime
        +getAppointmentDate() LocalDate
        +getAppointmentTimeOnly() LocalTime
    }

    class Prescription {
        <<Document>>
        -String id
        -String patientName
        -Long appointmentId
        -String medication
        -String dosage
        -String doctorNotes
        -int refillCount
        -Pharmacy pharmacy
    }

    class Pharmacy {
        <<embedded>>
        -String name
        -String location
    }

    class AppointmentDTO {
        <<DTO>>
        -Long id
        -Long doctorId
        -String doctorName
        -Long patientId
        -String patientName
        -String patientEmail
        -String patientPhone
        -String patientAddress
        -LocalDateTime appointmentTime
        -int status
        -String reasonForVisit
        -String notes
        -LocalDate appointmentDate
        -LocalTime appointmentTimeOnly
        -LocalDateTime endTime
    }

    class Login {
        <<DTO>>
        -String email
        -String password
    }

    Doctor "1" <-- "*" Appointment : doctor
    Patient "1" <-- "*" Appointment : patient
    Prescription *-- "1" Pharmacy
    Prescription ..> Appointment : appointmentId
    AppointmentDTO ..> Appointment : maps
```

### 4.2 Service layer

```mermaid
classDiagram
    direction TB

    class AppointmentService {
        <<interface>>
        +bookAppointment(AppointmentDTO) ResponseEntity~Integer~
        +updateAppointment(AppointmentDTO) ResponseEntity~Map~
        +cancelAppointment(Long, String) ResponseEntity~Map~
        +getAppointments(Long, LocalDate, String) ResponseEntity~List~
        +changeStatus(Long, Integer) ResponseEntity~Map~
    }

    class DoctorService {
        <<interface>>
        +getDoctorAvailability(Long, LocalDate) ResponseEntity~List~
        +saveDoctor(Doctor) ResponseEntity~Map~
        +updateDoctor(Doctor) ResponseEntity~Map~
        +deleteDoctor(Long) ResponseEntity~Map~
        +getDoctors() ResponseEntity~List~
        +validateDoctor(Login) ResponseEntity~Map~
        +filterDoctors(String, String, LocalDate, LocalDate) ResponseEntity~List~
        +findDoctorByName(String) ResponseEntity~List~
        +filterDoctorBy...(...) ResponseEntity~List~
    }

    class PatientService {
        <<interface>>
        +createPatient(Patient) ResponseEntity~String~
        +getPatientAppointment(Long) ResponseEntity~List~
        +filterByCondition(int, Long) ResponseEntity~List~
        +filterByDoctor(String, Long) ResponseEntity~List~
        +filterByDoctorAndCondition(String, Long, int) ResponseEntity~List~
        +getPatientDetails(String) ResponseEntity~Patient~
    }

    class PrescriptionService {
        <<interface>>
        +savePrescription(Prescription) ResponseEntity~Map~
        +getPrescription(Long) ResponseEntity
    }

    class AppointmentServiceIml
    class DoctorServiceImpl {
        -getAvailableTimes(Doctor, LocalDate) List~String~
        -split(TimeSlot) Stream~TimeSlot~
        -hasAvailabilityBetween(Doctor, LocalDateTime, LocalDateTime) boolean
    }
    class PatientServiceImpl
    class PrescriptionServiceImpl

    class ServiceImpl {
        +validateToken(String, String) ResponseEntity~Map~
        +validateAdmin(String, String) ResponseEntity~Map~
        +validatePatientLogin(Login) ResponseEntity~Map~
        +validatePatient(String, String) ResponseEntity~Boolean~
        +validateAppointment(AppointmentDTO) ResponseEntity~Integer~
        +filterDoctor(String, String, String) ResponseEntity~List~
        +filterPatient(String, String, Integer) ResponseEntity~List~
    }

    class TokenServiceImpl {
        +generateToken(String identifier, String role) String
        +extractEmail(String) String
        +extractRole(String) String
        +validateToken(String, String role) boolean
    }

    AppointmentService <|.. AppointmentServiceIml
    DoctorService <|.. DoctorServiceImpl
    PatientService <|.. PatientServiceImpl
    PrescriptionService <|.. PrescriptionServiceImpl

    ServiceImpl --> TokenServiceImpl
    ServiceImpl --> DoctorService
    ServiceImpl --> PatientService
    AppointmentServiceIml --> ServiceImpl : validateAppointment
    AppointmentServiceIml --> TokenServiceImpl
    DoctorServiceImpl --> TokenServiceImpl
    PatientServiceImpl --> TokenServiceImpl
```

### 4.3 Web layer and repositories

```mermaid
classDiagram
    direction LR

    class DashboardController {
        <<Controller>>
        +adminDashboard(token) String
        +doctorDashboard(token) String
    }
    class AdminController {
        <<RestController /admin>>
        +adminLogin(Admin)
    }
    class DoctorController {
        <<RestController /doctor>>
        +getDoctorAvailability(role, id, date, token)
        +getDoctors()
        +saveDoctor(token, Doctor)
        +doctorLogin(Login)
        +updateDoctor(token, Doctor)
        +deleteDoctor(id, token)
        +filter(name, time, speciality)
    }
    class PatientController {
        <<RestController /patient>>
        +getPatient(token)
        +createPatient(Patient)
        +login(Login)
        +getPatientAppointment(id, user, token)
        +filterPatientAppointment(condition, name, token)
    }
    class AppointmentController {
        <<RestController /appointments>>
        +getAppointments(date, patientName, token)
        +bookAppointment(token, AppointmentDTO)
        +updateAppointment(token, AppointmentDTO)
        +cancelAppointment(id, token)
    }
    class PrescriptionController {
        <<RestController /prescription>>
        +savePrescription(token, Prescription)
        +getPrescription(appointmentId, token)
    }

    class JpaRepository~T, ID~ {
        <<interface>>
    }
    class MongoRepository~T, ID~ {
        <<interface>>
    }
    class AdminRepository {
        +findByUsername(String) Admin
    }
    class DoctorRepository {
        +findByEmail(String) Doctor
        +findByNameLike(String) List
        +findBySpecialtyIgnoreCase(String) List
        +findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String, String) List
    }
    class PatientRepository {
        +findByEmail(String) Patient
        +existsByEmailOrPhone(String, String) boolean
    }
    class AppointmentRepository {
        +findByDoctorIdAndAppointmentTimeBetween(...) List
        +findByPatientId(Long) List
        +filterByDoctorNameAndPatientId(...) List
        +filterByDoctorNameAndPatientIdAndStatus(...) List
        +updateStatus(int, long)
        +deleteAllByDoctorId(Long)
    }
    class PrescriptionRepository {
        +findByAppointmentId(Long) Optional
        +existsByAppointmentId(Long) Boolean
    }

    JpaRepository <|-- AdminRepository
    JpaRepository <|-- DoctorRepository
    JpaRepository <|-- PatientRepository
    JpaRepository <|-- AppointmentRepository
    MongoRepository <|-- PrescriptionRepository
```

---

## 5. Key request flows

### 5.1 Admin login and dashboard

```mermaid
sequenceDiagram
    autonumber
    actor U as Admin
    participant FE as index.js
    participant AC as AdminController
    participant S as ServiceImpl
    participant AR as AdminRepository
    participant TS as TokenServiceImpl
    participant DC as DashboardController

    U->>FE: username + password
    FE->>AC: POST /admin/login
    AC->>S: validateAdmin(username, pwd)
    S->>AR: findByUsername(username)
    AR-->>S: Admin
    S->>TS: generateToken(username, "admin")
    TS-->>S: JWT (HS512, 7 days)
    S-->>AC: 200 {"token"}
    AC-->>FE: 200 {"token"}
    FE->>FE: localStorage: token, userRole
    FE->>DC: GET /adminDashboard/{token}
    DC->>S: validateToken(token, "admin")
    alt valid
        DC-->>U: admin/adminDashboard.html
    else invalid
        DC-->>U: redirect:/
    end
```

Doctor and patient logins follow the same pattern. They differ in the repository used and in where the user lands
(the doctor dashboard, or `loggedPatientDashboard.html`).

### 5.2 Booking an appointment

```mermaid
sequenceDiagram
    autonumber
    actor P as Patient
    participant FE as loggedPatient.js
    participant APC as AppointmentController
    participant S as ServiceImpl
    participant AS as AppointmentServiceIml
    participant DS as DoctorServiceImpl
    participant APR as AppointmentRepository

    P->>FE: picks doctor, date, slot, reason
    FE->>APC: POST /appointments/{token} (AppointmentDTO)
    APC->>APC: @Valid: ids, future time, reason 6-50 chars
    APC->>S: validateToken(token, "patient")
    APC->>AS: bookAppointment(dto)
    AS->>S: validateAppointment(dto)
    S->>DS: getDoctorAvailability(doctorId, date)
    DS->>APR: findByDoctorIdAndAppointmentTimeBetween(...)
    DS-->>S: free 1-hour slots
    alt doctor unknown
        S-->>AS: -1, then 404 "Invalid doctor id"
    else slot taken
        S-->>AS: 0, then 409 "Appointment time unavailable"
    else slot free
        AS->>APR: saveAndFlush(appointment)
        Note over APR: the unique key (doctor_id, appointment_time)<br/>catches a concurrent double booking, also 409
        AS-->>APC: 201
        APC-->>FE: 201 {"message": "Appointment booked successfully"}
    end
```

### 5.3 Writing a prescription (spans both databases)

```mermaid
sequenceDiagram
    autonumber
    actor D as Doctor
    participant FE as addPrescription.js
    participant PRC as PrescriptionController
    participant PRS as PrescriptionServiceImpl
    participant Mongo as MongoDB
    participant AS as AppointmentServiceIml
    participant MySQL as MySQL

    D->>FE: medication, dosage, notes
    FE->>PRC: POST /prescription/{token}
    PRC->>PRC: validateToken(token, "doctor")
    PRC->>PRS: savePrescription(p)
    PRS->>Mongo: existsByAppointmentId / save
    alt already exists
        PRS-->>FE: 400 "Prescription already exists"
    else created
        PRS-->>PRC: 201
        PRC->>AS: changeStatus(appointmentId, 1)
        AS->>MySQL: UPDATE appointment SET status = 1
        PRC-->>FE: 201 "Prescription created"
    end
```

The two writes are **not atomic**. If the MySQL update fails after the MongoDB insert, the prescription exists but the
appointment stays as scheduled. See the assessment below.

---

## 6. Security model

| Aspect | Implementation |
|---|---|
| Token | JWT signed with HS512 (`jwt.secret`, at least 64 bytes). `sub` = email or username, `role` claim, 7-day expiry. |
| Transport of the token | Last segment of the URL path (`/appointments/{token}`). The frontend keeps it in `localStorage`. |
| Authorization | Each protected endpoint calls `ServiceImpl.validateToken(token, role)`, which checks the signature, the expiry, the role claim, and that the user still exists. |
| Ownership | Patients can only read, update or cancel their own appointments. The patient is resolved from the token and compared with the resource. |
| Passwords | Write-only in JSON (`@JsonProperty(WRITE_ONLY)`). **Stored and compared in plain text.** |
| CORS | `WebConfig` allows every origin (`*`) for GET, POST, PUT and DELETE. |
| `JwtAuthenticationFilter` | Validates an `Authorization: Bearer` header if present and exposes the identity as request attributes. The frontend never sends that header, so in practice this filter is not used. |

There is no Spring Security. Authorization is done by hand in each controller method.

---

## 7. Persistence

| Concern | Choice |
|---|---|
| Schema management | `spring.jpa.hibernate.ddl-auto=update`: Hibernate creates and alters the tables. There are no migration scripts. |
| Validation | Bean Validation on entities and DTOs. It runs at the HTTP boundary (`@Valid`) **and** again when Hibernate persists. |
| Transactions | `@Transactional` on service methods. Writes that must report DB errors use `saveAndFlush` / `flush()` inside a `try`, so the exception is raised while it can still be caught. |
| Defaults | `Doctor.rating = 0.0`, `Doctor.clinicAdress` and `Prescription.pharmacy = "Not specified"` (`@Builder.Default`), so that entities are valid even when the frontend omits these fields. |
| Open session in view | Disabled (`spring.jpa.open-in-view=false`). Data is mapped to DTOs inside the transaction. |
| MongoDB | One collection, `prescriptions`, accessed through `MongoRepository`. `Pharmacy` is embedded. |

---

## 8. Error handling

Every error the frontend can display uses the key **`message`**.

```mermaid
flowchart LR
    E[Exception] --> V{Type}
    V -- MethodArgumentNotValidException --> V1["400: field messages joined,<br/>plus validationErrors"]
    V -- HttpMessageNotReadable --> V2["400: malformed body"]
    V -- MethodArgumentTypeMismatch --> V3["400: invalid path value"]
    V -- IllegalArgumentException --> V4["400: exception message"]
    V -- EntityNotFoundException --> V5["404: exception message"]
    V -- JwtException --> V6["401: invalid or expired token"]
    V -- anything else --> V7["500: Internal server error (logged)"]
```

Handled by `GlobalExceptionHandler`, which returns `ErrorResponse {timestamp, status, message, validationErrors}`.
Business outcomes such as "slot taken" or "already exists" are not exceptions. Services return them as
`ResponseEntity` values with the matching status code.

---

## 9. Frontend

| Page | Served as | Script | Talks to |
|---|---|---|---|
| `index.html` (role selection and logins) | static | `services/index.js`, `render.js` | `/admin/login`, `/doctor/login` |
| `templates/admin/adminDashboard.html` | Thymeleaf | `adminDashboard.js`, `doctorCard.js` | `/doctor/**` |
| `templates/doctor/doctorDashboard.html` | Thymeleaf | `doctorDashboard.js`, `patientRows.js` | `/appointments/{date}/...` |
| `pages/patientDashboard.html` (anonymous) | static | `patientDashboard.js` | `/doctor`, `/patient`, `/patient/login` |
| `pages/loggedPatientDashboard.html` | static | `loggedPatient.js` | `/doctor/**`, `/patient/{token}`, `POST /appointments/{token}` |
| `pages/patientAppointments.html` | static | `patientAppointment.js` | `/patient/{id}/patient/{token}`, `/patient/filter/...` |
| `pages/updateAppointment.html` | static | `updateAppointment.js` | `PUT /appointments/{token}` |
| `pages/patientRecord.html` | static | `patientRecordServices.js` | `/patient/{id}/doctor/{token}` |
| `pages/addPrescription.html` | static | `addPrescription.js` | `/prescription/**` |

The frontend uses native ES modules with no bundler. HTML that relies on inline handlers (`onclick="openModal()"`)
needs those functions on `window`. The modules that own them assign them explicitly (for example
`window.openModal`, `window.showBookingOverlay`).

---

## 10. Deployment and CI

```mermaid
flowchart LR
    subgraph compose["docker compose (app/docker-compose.yml)"]
        APP["app<br/>eclipse-temurin:17 JRE<br/>:8080"]
        MY[("mysql:8.0<br/>volume mysql-data")]
        MO[("mongo:6.0<br/>volume mongo-data<br/>+ mongo-seed.js")]
    end
    Host((Host)) -- "8080" --> APP
    Host -- "3307" --> MY
    Host -- "27018" --> MO
    APP -- "mysql:3306" --> MY
    APP -- "mongodb:27017" --> MO
```

- **Image.** A multi-stage Dockerfile builds with `maven:3.9.9-eclipse-temurin-17` and runs on
  `eclipse-temurin:17.0.15_6-jre`. Only the JAR goes into the runtime image.
- **Startup order.** The app waits for both databases to pass their healthchecks (`depends_on: service_healthy`).
- **Configuration.** Twelve-factor style: everything comes from environment variables (`.env` plus `environment:`
  overrides in Compose).

CI (GitHub Actions, on every push and PR):

```mermaid
flowchart LR
    Push([push / PR]) --> C["compile-backend<br/>mvn clean compile"]
    Push --> LB["lint-backend<br/>Checkstyle"]
    Push --> LF["lint-frontend<br/>HTMLHint, Stylelint, ESLint"]
    Push --> LD["lint-docker<br/>hadolint"]
    C --> R{{build status}}
    LD --> R
    LB -. warnings only .-> R
    LF -. warnings only .-> R
```