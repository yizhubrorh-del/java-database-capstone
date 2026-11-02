# Admin User Stories

## User Story 1: Admin Login

**Title:**
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**
1. The login page provides fields for username and password.
2. On valid credentials, the admin is authenticated and redirected to the admin dashboard.
3. On invalid credentials, an error message is shown and access is denied.

**Priority:** High
**Story Points:** 3
**Notes:**
- Passwords must be stored hashed, never in plain text.
- Consider locking the account or adding a delay after several failed attempts.

---

## User Story 2: Admin Logout

**Title:**
_As an admin, I want to log out of the portal, so that I can protect system access._

**Acceptance Criteria:**
1. A logout option is visible from every page of the admin dashboard.
2. Logging out invalidates the session/token on the server and clears it on the client.
3. After logout, the admin is redirected to the login page and cannot reach protected pages without logging in again.

**Priority:** High
**Story Points:** 1
**Notes:**
- Using the browser back button after logout must not show protected content.

---

## User Story 3: Add Doctor

**Title:**
_As an admin, I want to add doctors to the portal, so that patients can find them and book appointments._

**Acceptance Criteria:**
1. The admin can open a form to enter the doctor's details (name, specialty, email, phone, password, available times).
2. Required fields are validated before the doctor is saved.
3. On success, the new doctor appears in the doctor list and a confirmation message is shown.

**Priority:** High
**Story Points:** 3
**Notes:**
- A doctor's email must be unique; show an error if it already exists.
- Only authenticated admins can add doctors.

---

## User Story 4: Delete Doctor Profile

**Title:**
_As an admin, I want to delete a doctor's profile from the portal, so that I can remove doctors who no longer work with the clinic._

**Acceptance Criteria:**
1. Each doctor in the list has a delete option available to the admin.
2. The system asks for confirmation before deleting the profile.
3. After deletion, the doctor no longer appears in the list or in patient searches.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Define how existing appointments for that doctor are handled (cancel them or block deletion while future appointments exist).
- Only authenticated admins can delete doctors.

---

## User Story 5: Monthly Appointments Report

**Title:**
_As an admin, I want to run a stored procedure in the MySQL CLI to get the number of appointments per month, so that I can track usage statistics._

**Acceptance Criteria:**
1. A stored procedure exists in the database that returns the number of appointments grouped by month.
2. The admin can run it from the MySQL CLI with a `CALL` statement.
3. The result shows each month (and year) with its total number of appointments.

**Priority:** Low
**Story Points:** 2
**Notes:**
- Months with no appointments may be omitted or shown with zero.
- Optionally accept a year parameter to filter the results.

---

# Patient User Stories

## User Story 6: Browse Doctors Without Logging In

**Title:**
_As a patient, I want to view a list of doctors without logging in, so that I can explore my options before registering._

**Acceptance Criteria:**
1. The doctor list is available from the home page without authentication.
2. Each doctor shows at least name, specialty and available times.
3. Booking options require login, and the patient is prompted to log in or sign up when trying to book.

**Priority:** High
**Story Points:** 2
**Notes:**
- Do not expose sensitive doctor data (e.g. phone or email) to anonymous visitors.
- Consider filtering by name or specialty.

---

## User Story 7: Patient Sign Up

**Title:**
_As a patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**
1. The sign-up form asks for name, email, password and phone number.
2. Fields are validated (email format, password strength, required fields).
3. On success, the account is created and the patient can log in.

**Priority:** High
**Story Points:** 3
**Notes:**
- The email must be unique; show an error if it is already registered.
- Passwords must be stored hashed.

---

## User Story 8: Patient Login

**Title:**
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**
1. The login page accepts the patient's email and password.
2. On valid credentials, the patient is redirected to their dashboard.
3. On invalid credentials, an error message is shown and access is denied.

**Priority:** High
**Story Points:** 2
**Notes:**
- Consider limiting repeated failed login attempts.

---

## User Story 9: Patient Logout

**Title:**
_As a patient, I want to log out of the portal, so that I can secure my account._

**Acceptance Criteria:**
1. A logout option is visible from every page of the patient dashboard.
2. Logging out invalidates the session/token and clears it on the client.
3. After logout, the patient is redirected to the home page and cannot access protected pages.

**Priority:** High
**Story Points:** 1
**Notes:**
- Using the browser back button after logout must not show protected content.

---

## User Story 10: Book an Appointment

**Title:**
_As a patient, I want to log in and book an hour-long appointment, so that I can consult with a doctor._

**Acceptance Criteria:**
1. A logged-in patient can choose a doctor, a date and an available time slot.
2. Each appointment lasts one hour, and only free slots can be selected.
3. On success, the appointment is saved and a confirmation is shown.

**Priority:** High
**Story Points:** 5
**Notes:**
- Prevent double booking of the same doctor and time slot.
- Appointments cannot be booked in the past.

---

## User Story 11: View Upcoming Appointments

**Title:**
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly._

**Acceptance Criteria:**
1. The patient dashboard lists all future appointments of the logged-in patient.
2. Each appointment shows the doctor's name, date and time.
3. Appointments are sorted by date, with the closest first.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Show a friendly message when there are no upcoming appointments.
- A patient can only see their own appointments.

---

# Doctor User Stories

## User Story 12: Doctor Login

**Title:**
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**
1. The login page accepts the doctor's email and password.
2. On valid credentials, the doctor is redirected to their dashboard.
3. On invalid credentials, an error message is shown and access is denied.

**Priority:** High
**Story Points:** 2
**Notes:**
- Doctor accounts are created by the admin; there is no self sign-up for doctors.
- Consider limiting repeated failed login attempts.

---

## User Story 13: Doctor Logout

**Title:**
_As a doctor, I want to log out of the portal, so that I can protect my data._

**Acceptance Criteria:**
1. A logout option is visible from every page of the doctor dashboard.
2. Logging out invalidates the session/token and clears it on the client.
3. After logout, the doctor is redirected to the login page and cannot access protected pages.

**Priority:** High
**Story Points:** 1
**Notes:**
- Using the browser back button after logout must not show protected content.

---

## User Story 14: View Appointment Calendar

**Title:**
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**
1. The doctor dashboard shows the doctor's appointments by date.
2. Each appointment shows the patient's name, date and time.
3. The doctor can move between dates to see past and upcoming appointments.

**Priority:** High
**Story Points:** 3
**Notes:**
- A doctor can only see their own appointments.
- Show a friendly message when a date has no appointments.

---

## User Story 15: Mark Unavailability

**Title:**
_As a doctor, I want to mark my unavailability, so that patients only see the available slots._

**Acceptance Criteria:**
1. The doctor can select dates or time slots and mark them as unavailable.
2. Unavailable slots are not shown to patients when booking.
3. The doctor can remove an unavailability mark to make the slot bookable again.

**Priority:** High
**Story Points:** 3
**Notes:**
- Define what happens if a slot already has a booked appointment (block the change or notify the patient).
- Unavailability cannot be set in the past.

---

## User Story 16: Update Doctor Profile

**Title:**
_As a doctor, I want to update my profile with my specialization and contact information, so that patients have up-to-date information._

**Acceptance Criteria:**
1. The doctor can edit their specialization, phone number and other contact details.
2. Fields are validated before saving (required fields, phone and email format).
3. On success, the changes are saved and shown immediately in the doctor list.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- If the email can be changed, it must remain unique.
- A doctor can only edit their own profile.

---

## User Story 17: View Patient Details for Upcoming Appointments

**Title:**
_As a doctor, I want to view the patient details for my upcoming appointments, so that I can be prepared._

**Acceptance Criteria:**
1. From an upcoming appointment, the doctor can open the patient's details.
2. The details include at least the patient's name, contact information and the reason for the appointment, if provided.
3. Only patients with an appointment with that doctor are visible to them.

**Priority:** Medium
**Story Points:** 2
**Notes:**
- Patient data is sensitive; access must be restricted to the assigned doctor.
