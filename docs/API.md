# API Reference

Base URL: `http://localhost:8080`

## Conventions

- **Authentication.** Protected endpoints receive the JWT as the **last path segment** (`.../{token}`). The token
  is issued by the login endpoints, is valid for 7 days, and carries a `role` claim. Each endpoint checks that the
  role matches. Otherwise it returns `401 {"message": "Invalid or expired token"}`.
- **Messages.** Every success and error response that is not a data payload has the shape
  `{"message": "..."}`. Exceptions handled globally also add `timestamp` and `status`, and validation errors add
  `validationErrors` (field → message).
- **Empty filters.** Filter endpoints use path segments, so an empty filter is sent as the literal string
  `null` (for example `/doctor/filter/null/AM/null`).
- **Status codes shared by all endpoints.** `400` for invalid body or path values, `401` for an invalid or
  expired token, `404` when an entity is missing, `500` for an unexpected error.

## Pages (Thymeleaf MVC)

| Method | Path | Role | Result |
|---|---|---|---|
| GET | `/adminDashboard/{token}` | admin | Renders `admin/adminDashboard`, or redirects to `/` if the token is invalid |
| GET | `/doctorDashboard/{token}` | doctor | Renders `doctor/doctorDashboard`, or redirects to `/` if the token is invalid |

Static pages (`/`, `/pages/*.html`) are served directly from `src/main/resources/static`.

## Admin: `/admin` (alias `/api/admin`)

| Method | Path | Role | Body | Success | Errors |
|---|---|---|---|---|---|
| POST | `/login` | public | `{"username", "password"}` | `200 {"token"}` | `401 Incorrect password or username` |

## Doctors: `/doctor` (alias `/api/doctor`)

| Method | Path | Role | Body | Success | Errors |
|---|---|---|---|---|---|
| GET | `/` | public | | `200 {"doctors": [Doctor]}` | |
| GET | `/filter/{name}/{time}/{speciality}` | public | | `200 {"doctors": [Doctor]}` | `400` if `time` is not `AM`, `PM` or `null` |
| GET | `/availability/{user}/{doctorId}/{date}/{token}` | `{user}` | | `200 {"availability": ["09:00-10:00", ...]}` | `404` unknown doctor |
| POST | `/login` | public | `{"email", "password"}` | `200 {"token"}` | `401 Invalid email or password` |
| POST | `/{token}` or `/save/{token}` | admin | `Doctor` | `201 Doctor added to db` | `409 Doctor already exists`, `500` |
| PUT | `/{token}` | admin | `Doctor` (with `id`) | `200 Doctor updated` | `404 Doctor not found`, `500` |
| DELETE | `/{id}/{token}` | admin | | `200 Doctor deleted successfully` | `404 Doctor not found with id {id}`, `500` |

Notes:
- `time=AM` keeps doctors that have a slot starting before 12:00. `PM` keeps those with a slot starting at 12:00 or later.
- `date` uses ISO format (`2026-11-20`). Availability splits each configured range into 1-hour slots and removes the ones
  that overlap existing appointments.
- Deleting a doctor also deletes their appointments.
- `clinicAdress` defaults to `"Not specified"` and `rating` to `0.0` when omitted.

## Patients: `/patient`

| Method | Path | Role | Body | Success | Errors |
|---|---|---|---|---|---|
| POST | `/` | public | `Patient` | `201 Signup successful` | `409 Patient with email id or phone no already exist`, `500 Internal server error` |
| POST | `/login` | public | `{"email", "password"}` | `200 {"token"}` | `401 Incorrect email or password` |
| GET | `/{token}` | patient | | `200 {"patient": Patient}` | |
| GET | `/{id}/{user}/{token}` | `{user}` (patient or doctor) | | `200 {"appointments": [AppointmentDTO]}` | `403` if a patient requests another patient's appointments |
| GET | `/filter/{condition}/{name}/{token}` | patient | | `200 {"appointments": [AppointmentDTO]}` | |

Notes:
- `condition` is `future` (status 0), `past` (status 1) or `null` (any). `name` filters by doctor name.
- The patient is always resolved from the token, so a patient can only filter their own appointments.

## Appointments: `/appointments`

| Method | Path | Role | Body | Success | Errors |
|---|---|---|---|---|---|
| GET | `/{date}/{patientName}/{token}` | doctor | | `200 {"appointments": [AppointmentDTO]}` | `404 Doctor not found` |
| POST | `/{token}` | patient | `AppointmentDTO` | `201 Appointment booked successfully` | `404 Invalid doctor id`, `409 Appointment time unavailable` |
| PUT | `/{token}` | patient | `AppointmentDTO` (with `id`) | `200 Appointment updated correctly` | `400 Appointment id is required`, `403` not the owner, `409` completed appointment or slot taken |
| DELETE | `/{id}/{token}` | patient | | `200 Appointment cancelled successfully` | `403` not the owner, `409 Can't delete a completed appointment` |

Notes:
- `GET` returns the logged-in doctor's appointments for that day. `patientName` filters by patient name
  (partial, case-insensitive) or is `null`.
- Request body for booking or updating:

```json
{
  "id": 12,
  "doctorId": 3,
  "patientId": 7,
  "appointmentTime": "2026-11-20T09:00:00",
  "status": 0,
  "reasonForVisit": "Annual check-up",
  "notes": "Fasting since 8 pm"
}
```

  `id` is only required when updating. `doctorId`, `patientId` and `appointmentTime` are required, and
  `appointmentTime` must be in the future. `reasonForVisit` must be 6 to 50 characters. `notes` is optional, up to
  100 characters. `status` is 0 or 1.

## Prescriptions: `/prescription`

| Method | Path | Role | Body | Success | Errors |
|---|---|---|---|---|---|
| POST | `/{token}` | doctor | `Prescription` | `201 Prescription created` | `400 Prescription already exists` |
| GET | `/{appointmentId}/{token}` | doctor | | `200 {"prescription": [Prescription]}` | `404 Prescription not found` |

Notes:
- Saving a prescription marks its appointment as completed (`status = 1`). This only happens if the save succeeds.
- `pharmacy` defaults to `{"name": "Not specified", "location": "Not specified"}` when omitted.
- There is at most one prescription per appointment. The response wraps it in a list for frontend compatibility.

## Data shapes

**AppointmentDTO** (response). Same as the request body, plus:
`doctorName`, `patientName`, `patientEmail`, `patientPhone`, `patientAddress`, `appointmentDate`,
`appointmentTimeOnly`, and `endTime` (start + 1 hour).

**Doctor**: `id`, `name`, `speciality`, `email`, `phone`, `availableTimes` (`["09:00-10:00", ...]`),
`yearsOfExperience`, `clinicAdress`, `rating`. `password` is write-only and is never serialized.

**Patient**: `id`, `name`, `email`, `phone`, `address`, `dateOfBirth`, `emergencyContact`,
`insuranceProvider`. `password` is write-only.

**Prescription**: `id`, `patientName`, `appointmentId`, `medication`, `dosage`, `doctorNotes`, `refillCount`,
and `pharmacy {name, location}`.

Field constraints are listed in [schema-design.md](../schema-design.md).
