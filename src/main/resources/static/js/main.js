import { login, register, logout, currentUserLabel } from "./auth.js";
import { renderPerformances } from "./performances.js";

const $ = (s, r=document) => r.querySelector(s);
const $$ = (s, r=document) => Array.from(r.querySelectorAll(s));

function showView(id) {
  $$(".view").forEach(v => v.classList.add("hidden"));
  const target = document.getElementById(id);
  if (target) target.classList.remove("hidden");
}

function updateUserBox() {
  $("#user-info").textContent = currentUserLabel();
  const hasToken = !!localStorage.getItem("access_token");
  $("#btn-logout").classList.toggle("hidden", !hasToken);
}

function wireTabs() {
  $$(".tabs [data-target], a[data-target]").forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      showView(btn.getAttribute("data-target"));
    });
  });
}

function wireLogout() {
  $("#btn-logout").addEventListener("click", () => {
    logout();
    updateUserBox();
    // μετά το logout δείξε performances (θα δείξει μήνυμα αν είναι protected)
    showView("view-performances");
    $("#perf-msg").textContent = "Αποσυνδέθηκες.";
    $("#perf-msg").className = "msg";
  });
}

function wireLoginForm() {
  const form = $("#form-login");
  const msg = $("#login-msg");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    msg.textContent = "Σύνδεση...";
    msg.className = "msg";
    const fd = new FormData(form);
    try {
      await login(fd.get("username"), fd.get("password"));
      msg.textContent = "Επιτυχής σύνδεση.";
      msg.classList.add("success");
      updateUserBox();
      // πήγαινε στις performances
      showView("view-performances");
      // φρεσκάρισμα λίστας
      $("#btn-search").click();
    } catch (err) {
      msg.textContent = "Αποτυχία σύνδεσης.";
      msg.classList.add("error");
    }
  });
}

function wireRegisterForm() {
  const form = $("#form-register");
  const msg = $("#register-msg");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    msg.textContent = "Δημιουργία λογαριασμού...";
    msg.className = "msg";
    const fd = new FormData(form);
    try {
      await register({ username: fd.get("username"), password: fd.get("password"), email: fd.get("email") || undefined });
      msg.textContent = "Ο λογαριασμός δημιουργήθηκε. Μπορείς να κάνεις login.";
      msg.classList.add("success");
      showView("view-login");
    } catch (err) {
      msg.textContent = "Αποτυχία εγγραφής.";
      msg.classList.add("error");
    }
  });
}

function wirePerformances() {
  const list = $("#perf-list");
  const msg = $("#perf-msg");
  const search = $("#search-perf");

  async function load(term="") {
    await renderPerformances(list, msg, term);
  }

  $("#btn-search").addEventListener("click", () => load(search.value.trim()));
  // αρχικό load
  load();
}

document.addEventListener("DOMContentLoaded", () => {
  wireTabs();
  wireLogout();
  wireLoginForm();
  wireRegisterForm();
  wirePerformances();
  updateUserBox();

  // default view
  showView("view-performances");
});
