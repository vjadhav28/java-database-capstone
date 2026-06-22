/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/
import { showOverlay } from "./loggedPatient.js";
import { deleteDoctor } from "./doctorServices.js";
import { getPatient } from "./patientServices.js";

export function getDoctorCard(doctor) {
  // Create the main container for the doctor card
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  // Retrieve the current user role from localStorage
  const currentUserRole = localStorage.getItem("userRole");

  // Create a div to hold doctor information
  const doctorInfo = document.createElement("div");
  doctorInfo.classList.add("doctor-info");

  // Create and set the doctor's name
  const name = document.createElement("h3");
  name.textContent = doctor.name;

  // Create and set the doctor's specialization
  const specialization = document.createElement("p");
  specialization.textContent = doctor.specialization;

  // Create and set the doctor's email
  const email = document.createElement("p");
  email.textContent = doctor.email;

  // Create and list available appointment times
  const timesContainer = document.createElement("div");
  timesContainer.classList.add("available-times");

  const timesLabel = document.createElement("strong");
  timesLabel.textContent = "Available Times:";
  timesContainer.appendChild(timesLabel);

  const timesList = document.createElement("ul");
  doctor.availableTimes.forEach((time) => {
    const timeItem = document.createElement("li");
    timeItem.textContent = time;
    timesList.appendChild(timeItem);
  });
  timesContainer.appendChild(timesList);

  // Append all info elements to the doctor info container
  doctorInfo.appendChild(name);
  doctorInfo.appendChild(specialization);
  doctorInfo.appendChild(email);
  doctorInfo.appendChild(timesContainer);

  // Create a container for card action buttons
  const actionsContainer = document.createElement("div");
  actionsContainer.classList.add("card-actions");

  // === ADMIN ROLE ACTIONS ===
  if (currentUserRole === "admin") {
    // Create a delete button
    const deleteButton = document.createElement("button");
    deleteButton.textContent = "Delete";
    deleteButton.classList.add("delete-btn");

    // Add click handler for delete button
    deleteButton.addEventListener("click", async () => {
      // Get the admin token from localStorage
      const adminToken = localStorage.getItem("authToken");

      try {
        // Call API to delete the doctor
        const result = await deleteDoctor(doctor.id, adminToken);

        // Show result and remove card if successful
        alert(result.message);
        if (result.success) {
          card.remove();
        }
      } catch (error) {
        console.error("Error deleting doctor:", error);
        alert("Failed to delete doctor. Please try again.");
      }
    });

    // Add delete button to actions container
    actionsContainer.appendChild(deleteButton);
  }

  // === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
  else if (!currentUserRole) {
    // Create a book now button
    const bookButton = document.createElement("button");
    bookButton.textContent = "Book Now";
    bookButton.classList.add("book-btn");

    // Alert patient to log in before booking
    bookButton.addEventListener("click", () => {
      alert("Please log in to book an appointment.");
    });

    // Add button to actions container
    actionsContainer.appendChild(bookButton);
  }

  // === LOGGED-IN PATIENT ROLE ACTIONS === 
  else if (currentUserRole === "patient") {
    // Create a book now button
    const bookButton = document.createElement("button");
    bookButton.textContent = "Book Now";
    bookButton.classList.add("book-btn");

    // Handle booking logic for logged-in patient   
    bookButton.addEventListener("click", async () => {
      // Redirect if token not available
      const token = localStorage.getItem("authToken");
      if (!token) {
        alert("Please log in to book an appointment.");
        return;
      }

      try {
        // Fetch patient data with token
        const patientData = await getPatient(token);

        // Show booking overlay UI with doctor and patient info
        showOverlay(doctor, patientData);
      } catch (error) {
        console.error("Error fetching patient data:", error);
        alert("Failed to fetch patient data. Please try again.");
      }
    });

    // Add button to actions container
    actionsContainer.appendChild(bookButton);
  }

  // Append doctor info and action buttons to the card
  card.appendChild(doctorInfo);
  card.appendChild(actionsContainer);

  // Return the complete doctor card element
  return card;
}     
