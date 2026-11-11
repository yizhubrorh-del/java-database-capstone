// patientServices
import { API_BASE_URL } from "../config/config.js";
const PATIENT_API = API_BASE_URL + '/patient'


//For creating a patient in db
export async function patientSignup(data) {
  try {
    // Send the patient details as JSON
    const response = await fetch(`${PATIENT_API}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
      }
    );
    // Parse the response and check status
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.message);
    }
    // Return a structured result
    return { success: response.ok, message: result.message }
  }
  catch (error) {
    // Handle network/server errors
    console.error("Error :: patientSignup :: ", error)
    return { success: false, message: error.message }
  }
}

//For logging in patient
export async function patientLogin(data) {
  // Log input during development (remove in production)
  console.log("patientLogin :: ", data)
  // Send credentials and return the raw response so the caller can read status/token
  return await fetch(`${PATIENT_API}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });
}

// For getting patient data (name ,id , etc ). Used in booking appointments
export async function getPatientData(token) {
  try {
    // Request the logged-in patient's details using the token
    const response = await fetch(`${PATIENT_API}/${token}`);
    const data = await response.json();
    // Return the patient object, or null if the request failed
    if (response.ok) return data.patient;
    return null;
  } catch (error) {
    console.error("Error fetching patient details:", error);
    return null;
  }
}

// the Backend API for fetching the patient record(visible in Doctor Dashboard) and Appointments (visible in Patient Dashboard) are same based on user(patient/doctor).
export async function getPatientAppointments(id, token, user) {
  try {
    // Build a URL that works for both patient and doctor dashboards
    const response = await fetch(`${PATIENT_API}/${id}/${user}/${token}`);
    const data = await response.json();
    // Return the appointments, or null if the request failed
    if (response.ok) {
      return data.appointments;
    }
    return null;
  }
  catch (error) {
    console.error("Error fetching patient appointments:", error);
    return null;
  }
}

export async function filterAppointments(condition, name, token) {
  try {
    // Empty filters are sent as "null" so the route never has empty segments
    const c = condition || "null";
    const n = name || "null";
    // Request the filtered appointments
    const response = await fetch(`${PATIENT_API}/filter/${encodeURIComponent(c)}/${encodeURIComponent(n)}/${token}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Return the filtered appointments, or an empty list on failure
    if (response.ok) {
      const data = await response.json();
      return data;
    } else {
      console.error("Failed to fetch appointments:", response.statusText);
      return { appointments: [] };
    }
  } catch (error) {
    // Unexpected error: log and alert the user
    console.error("Error:", error);
    alert("Something went wrong!");
    return { appointments: [] };
  }
}
