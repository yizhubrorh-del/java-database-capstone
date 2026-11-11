import { openModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';

const ADMIN_API = `${API_BASE_URL}/api/admin/login`;
const DOCTOR_API = `${API_BASE_URL}/api/doctor/login`;

window.onload = function () {
  const adminLoginBtn = document.getElementById("adminLogin");
  const doctorLoginBtn = document.getElementById("doctorLogin");

  if(adminLoginBtn){
    adminLoginBtn.addEventListener("click", () => {
      openModal('adminLogin');
    });
  }

  if(doctorLoginBtn){
    doctorLoginBtn.addEventListener("click", () => {
      openModal('doctorLogin');
    });
  }
};

window.adminLoginHandler = async function (event) {
  if(event) event.preventDefault();

  const usernameInput = document.getElementById("adminUsername")?.value;
  const passwordInput = document.getElementById("adminPassword")?.value;

  const adminCredentials = {
    username: usernameInput,
    password: passwordInput
  };

  try{
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(adminCredentials)
    });

    if(response.ok){
      const data = await response.json();

      localStorage.setItem("token", data.token);
      localStorage.setItem("userRole", "admin");

      if(typeof selectRole === "function"){
        selectRole("admin");
      }else{
        window.location.href = '../../../templates/admin/adminDashboard.html';
      }
    }else{
      alert("Invalid admin credentials. Please try again");
    }
  }catch(error){
    console.error("Admin login error: ", error);
    alert("An error occurred while connecting to the server. Please try again later.");
  }
};

window.doctorLoginHandler = async function(event){
  if(event) event.preventDefault();

  const emailInput = document.getElementById("doctorEmail")?.value;
  const passwordInput = document.getElementById("doctorPassword").value;

  const doctorCredentials = {
    email: emailInput,
    password: passwordInput
  };

  try{
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(doctorCredentials)
    });

    if(response.ok){
      const data = await response.json();

      localStorage.setItem("token", data.token);
      localStorage.setItem("userRole", "doctor");

      if(typeof selectRole === "function"){
        selectRole('doctor');
      }else{
        window.location.href = "../../../templates/doctor/doctorDashboard.html";
      }
    }else{
      alert("Invalid doctor credentials. Please try again.");
    }
  }catch(error){
    console.error("Doctor login error: ", error);
    alert("An error occurred while connecting to the server. Please try again later.");
  }
};