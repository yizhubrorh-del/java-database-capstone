import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';

export function createDoctorCard(doctor){
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    const role = localStorage.getItem("userRole");

    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name;

    const specialization = document.createElement("p");
    specialization.textContent = `Specialty: ${doctor.speciality || 'N/A'}`;

    const email = document.createElement("p");
    email.textContent = `Email: ${doctor.email || 'N/A'}`;

    const availability = document.createElement("p");
    const availText = Array.isArray(doctor.availableTimes) && doctor.availableTimes.length > 0
        ? doctor.availableTimes.join(", ")
        : 'No availability';
    availability.textContent = `Availability: ${availText}`;

    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    if(role === "admin"){
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("addminBtn", "delete-btn");

        removeBtn.addEventListener("click", async () => {
            if(confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)){
                try{
                    const token = localStorage.getItem("token");
                    await deleteDoctor(doctor.id, token);
                    card.remove();
                }catch (error){
                    console.error("Failed to delete doctor: ", error);
                    alert("Error deletin doctor. Please try again.");
                }
            }
        });

        actionsDiv.appendChild(removeBtn);
    }else if(role === "patient"){
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book now";
        bookNow.classList.add("adminBtn", "book-btn");

        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });
        actionsDiv.appendChild(bookNow);
    }else if(role === "loggedPatient"){
         const bookNow = document.createElement("button");
        bookNow.textContent = "Book now";
        bookNow.classList.add("adminBtn", "book-btn");
        
        bookNow.addEventListener("click", async () => {
            try{
                const token = localStorage.getItem("token");
                const patientData = await getPatientData(token);

                if(typeof showBookingOverlay === "function"){
                    showBookingOverlay(e, doctor, patientData);
                }else{
                    console.warn("showBookingOverlay is not defined yet.");
                }
            }catch (error){
                console.error("Failed to fetch patient data for booking: ", error);
                    alert("Session error. Please log in again.");
            }
        });

        actionsDiv.appendChild(bookNow);
    }

    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}