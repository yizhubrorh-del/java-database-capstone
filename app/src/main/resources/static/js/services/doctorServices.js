import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + '/doctor';

/**
 * Fetch the list of all doctors from the API
 * @returns {Promise<Array>} Array of doctors or empty array on error
 */
export async function getDoctors(){
  try{
    const response = await fetch(DOCTOR_API);
    if(!response.ok){
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    return data.doctors || data;
  } catch (error){
    console.error("Error fetching doctors: ", error);
    return [];
  } 
}

/**
 * Delete a specific doctor using their ID and an authentication token
 * @param {string|number} doctorId 
 * @param {string} token 
 * @returns {Promise<Object>} Object with success status and message
 */
export async function deleteDoctor(doctorId, token) {
  try{
    const response = await fetch(`${DOCTOR_API}/${doctorId}/${token}`, {
      method: "DELETE"
    });

    const data = await response.json();

    if(response.ok){
      return { 
        success: true,
        message: data.message || "Doctor deleted successfully"
      };
    }else{
      return{
        success: false,
        message: data.message || "Failed to delete doctor"
      };
    } 
  }catch(error){
    console.error("Error deleting doctor: ", error);
    return{
      success: false,
      message: "Network or server error occurred while deleting."
    };
  }
}

/**
 * Save (create) a new doctor using a POST request
 * @param {Object} doctorObject 
 * @param {string} token 
 * @returns {Promise<Object>} Object with success status and message
 */
export async function saveDoctor(doctorObject, token) {
  try{
    const response = await fetch(`${DOCTOR_API}/save/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(doctorObject)
    });

    const data = await response.json();

    if(response.ok){
      return{
        success: true,
        message: data.message || "Doctor saved successfully"
      };
    }else{
      return{
        success: false,
        message: data.message || "Failed to save doctor"
      };
    }
  }catch(error){
    console.error("Error saving doctor: ", error);
    return{
      success: false,
      message: "Network or server error occurred while saving."
    };
  }
}


/**
 * Fetch doctors based on filtering criteria (name, time, and specialty)
 * @param {string} name 
 * @param {string} time 
 * @param {string} specialty 
 * @returns {Promise<Object>} Object containing the doctors array
 */
export async function filterDoctors(name="", time="", specialty ="") {
  try{

    const n = name || "null";
    const t = time || "null";
    const s = specialty || "null";
    const response = await fetch(`${DOCTOR_API}/filter/${encodeURIComponent(n)}/${encodeURIComponent(t)}/${encodeURIComponent(s)}`);

    if(response.ok){
      const data = await response.json();
      return data;
    }else{
      console.error("Failed to filter doctors. Server returned status:", response.status);
      return {doctors: []};
    }
  }catch(error){
    console.error("Error filtering doctors: ", error);
    alert("An error occurred while filtering doctors. Please try again.");
    return { doctors: []};
  }
}