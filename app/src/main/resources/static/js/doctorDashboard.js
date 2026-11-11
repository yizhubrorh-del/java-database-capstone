// doctorDashboard.js
// Handles the doctor dashboard: lists appointments by date and filters by patient name.
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// Global state
const tableBody = document.getElementById("patientTableBody");
const today = new Date().toISOString().split("T")[0]; // YYYY-MM-DD
let selectedDate = today;
const token = localStorage.getItem("token");
let patientName = null;

// Search bar: filter by patient name ("null" means no filter for the backend)
document.getElementById("searchBar")?.addEventListener("input", (event) => {
  const value = event.target.value.trim();
  patientName = value.length > 0 ? value : "null";
  loadAppointments();
});

// "Today's Appointments" button: reset the date to today
document.getElementById("todayButton")?.addEventListener("click", () => {
  selectedDate = today;
  document.getElementById("datePicker").value = today;
  loadAppointments();
});

// Date picker: load appointments for the selected date
document.getElementById("datePicker")?.addEventListener("change", (event) => {
  selectedDate = event.target.value;
  loadAppointments();
});

/**
 * Fetch and display appointments for selectedDate, optionally filtered by patientName.
 */
async function loadAppointments() {
  try {
    // Step 1: fetch appointments from the backend
    const response = await getAllAppointments(selectedDate, patientName ?? "null", token);
    const appointments = response?.appointments ?? [];

    // Step 2: clear the table
    tableBody.innerHTML = "";

    // Step 3: no appointments -> show a message row
    if (appointments.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">No Appointments found for today.</td></tr>`;
      return;
    }

    // Step 4: one row per appointment
    appointments.forEach((appointment) => {
      const patient = {
        id: appointment.patientId,
        name: appointment.patientName,
        phone: appointment.patientPhone,
        email: appointment.patientEmail,
      };
      const row = createPatientRow(patient, appointment.id, appointment.doctorId);
      tableBody.appendChild(row);
    });
  } catch (error) {
    // Step 5: fallback message on error
    console.error("Error loading appointments:", error);
    tableBody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td></tr>`;
  }
}

// Initial render: today's appointments
document.addEventListener("DOMContentLoaded", () => {
  if (typeof renderContent === "function") renderContent();
  document.getElementById("datePicker").value = today;
  loadAppointments();
});
