import { openModal } from "./components/modals.js";

document.addEventListener("DOMContentLoaded", function() {
  const adminBtn = document.getElementById("adminBtn");
  const patientBtn = document.getElementById("patientBtn");
  const doctorBtn = document.getElementById("doctorBtn");

  if (adminBtn) {
    adminBtn.addEventListener("click", () => openModal("adminLogin"));
  }

  if (patientBtn) {
    patientBtn.addEventListener("click", () => openModal("patientLogin"));
  }

  if (doctorBtn) {
    doctorBtn.addEventListener("click", () => openModal("doctorLogin"));
  }
});
