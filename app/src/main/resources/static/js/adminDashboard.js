// adminDashboard.js
// Handles the admin dashboard: loads doctor cards, filters them and adds new doctors.
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// header.js renders the "Add Doctor" button with an inline onclick="openModal(...)",
// which needs openModal to be global because this file is an ES module.
window.openModal = openModal;

document.addEventListener("DOMContentLoaded", () => {
  // Bind the "Add Doctor" button (if the header has already rendered it)
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", () => openModal("addDoctor"));
  }

  // Bind search bar and filter dropdowns
  document.getElementById("searchBar")?.addEventListener("input", filterDoctorsOnChange);
  document.getElementById("filterTime")?.addEventListener("change", filterDoctorsOnChange);
  document.getElementById("filterSpecialty")?.addEventListener("change", filterDoctorsOnChange);

  // Initial load of all doctors
  loadDoctorCards();
});

/**
 * Fetch all doctors and display them as cards.
 */
async function loadDoctorCards() {
  try {
    // Fetch the doctor list from the backend
    const doctors = await getDoctors();
    // Render them in the content area
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Failed to load doctors:", error);
  }
}

/**
 * Filter doctors by name, available time and specialty.
 */
async function filterDoctorsOnChange() {
  try {
    // Read current filter values and normalize empty ones to null
    const name = document.getElementById("searchBar").value.trim() || null;
    const time = document.getElementById("filterTime").value || null;
    const specialty = document.getElementById("filterSpecialty").value || null;

    // Ask the backend for matching doctors
    const response = await filterDoctors(name, time, specialty);
    const doctors = response?.doctors ?? [];

    // Render results or a fallback message
    if (doctors.length > 0) {
      renderDoctorCards(doctors);
    } else {
      document.getElementById("content").innerHTML =
        "<p>No doctors found with the given filters.</p>";
    }
  } catch (error) {
    console.error("Failed to filter doctors:", error);
    alert("❌ An error occurred while filtering doctors.");
  }
}

/**
 * Render a list of doctors into the #content div.
 * @param {Array} doctors
 */
export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  // Clear previous content
  contentDiv.innerHTML = "";
  // Append one card per doctor
  (doctors || []).forEach((doctor) => {
    contentDiv.appendChild(createDoctorCard(doctor));
  });
}

/**
 * Collect the "Add Doctor" modal form data and save the new doctor.
 * Must be global: modals.js binds it as the Save button handler.
 */
window.adminAddDoctor = async function () {
  // Collect input values from the modal form
  const name = document.getElementById("doctorName").value.trim();
  const speciality = document.getElementById("specialization").value;
  const email = document.getElementById("doctorEmail").value.trim();
  const password = document.getElementById("doctorPassword").value;
  const phone = document.getElementById("doctorPhone").value.trim();
  const availableTimes = Array.from(
    document.querySelectorAll('input[name="availability"]:checked')
  ).map((checkbox) => checkbox.value);

  // Verify the admin is authenticated
  const token = localStorage.getItem("token");
  if (!token) {
    alert("❌ Admin not authenticated. Please log in again.");
    return;
  }

  // Build the doctor object (field names match the Doctor model)
  const doctor = { name, speciality, email, password, phone, availableTimes };

  // Send it to the backend
  const { success, message } = await saveDoctor(doctor, token);

  if (success) {
    alert(`✅ ${message}`);
    document.getElementById("modal").style.display = "none";
    await loadDoctorCards();
  } else {
    alert(`❌ ${message}`);
  }
};
