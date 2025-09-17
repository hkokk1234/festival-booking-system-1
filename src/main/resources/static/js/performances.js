// /static/js/performances.js
console.log("performances.js loaded");

// ---- Config ----
const CFG = window.APP_CONFIG || {};
const API_BASE = (CFG.API_BASE || "/api").replace(/\/+$/, "");
const PERF_PATH = (CFG.RESOURCES?.PERFORMANCES) || "/performances/approved";

// ---- DOM helpers ----
const $ = (s) => document.querySelector(s);
const el = (t, p = {}, c = []) => {
  const n = document.createElement(t);
  Object.entries(p).forEach(([k, v]) => (k === "class" ? (n.className = v) : n.setAttribute(k, v)));
  (Array.isArray(c) ? c : [c]).forEach((x) => n.appendChild(typeof x === "string" ? document.createTextNode(x) : x));
  return n;
};
const setMsg = (t, kind) => {
  const m = $("#msg");
  m.textContent = t || "";
  m.className = "msg" + (kind ? " " + kind : "");
};

// ---- Data helpers ----
function extractTime(perf) {
  const candidates = [
    perf.scheduledTime,
    perf.performanceTime,
    perf.startTime,
    perf.dateTime,
    perf.time,
    perf.timeSlot,
    perf.performanceDateTime,
  ].filter(Boolean);

  if ((!candidates || candidates.length === 0) &&
      Array.isArray(perf.preferredPerformanceSlots) &&
      perf.preferredPerformanceSlots.length) {
    const sorted = [...perf.preferredPerformanceSlots].sort();
    candidates.push(sorted[0]);
  }

  const iso =
    candidates.find((v) => typeof v === "string" && /^\d{4}-\d{2}-\d{2}T/.test(v)) ||
    candidates.find((v) => typeof v === "string") ||
    null;

  if (!iso) return null;
  const first = String(iso).split(/\s*-\s*/)[0];
  const d = new Date(first);
  return isNaN(d.getTime()) ? null : d;
}
const statusIsApproved = (p) => String(p.status || "").toUpperCase() === "APPROVED";

function card(perf) {
  const title = perf.name || perf.title || perf.performanceName || "Untitled";
  const artist = perf.mainArtist?.username || perf.artist || perf.performer || "—";
  const when = extractTime(perf);
  const whenTxt = when
    ? new Intl.DateTimeFormat("el-GR", { dateStyle: "medium", timeStyle: "short" }).format(when)
    : "—";
  const venue = perf.festival?.name || perf.venue || perf.stage || "—";
  const genre = perf.genre || perf.category || null;

  return el("div", { class: "card" }, [
    el("h3", {}, title),
    el("div", { class: "meta" }, `Καλλιτέχνης: ${artist}`),
    el("div", { class: "meta" }, `Φεστιβάλ/Χώρος: ${venue}`),
    el("div", { class: "meta" }, `Ώρα εμφάνισης: ${whenTxt}`),
    genre ? el("span", { class: "badge" }, genre) : null,
  ]);
}

// ---- API (καθαρό fetch) ----
async function fetchApproved(term = "") {
  const url = new URL(API_BASE + PERF_PATH, location.origin);
  url.searchParams.set("status", "APPROVED");
  if (term) url.searchParams.set("q", term);

  const headers = {};
  const t = localStorage.getItem("jwt");
  if (t) headers.Authorization = "Bearer " + t;

  const res = await fetch(url, { headers, credentials: "include" });
  if (!res.ok) {
    const err = new Error(res.statusText);
    err.status = res.status;
    throw err;
  }
  const ct = res.headers.get("content-type") || "";
  const data = ct.includes("application/json") ? await res.json() : null;

  const raw = Array.isArray(data) ? data : (Array.isArray(data?.content) ? data.content : []);
  const items = raw.filter(statusIsApproved);

  items.sort((a, b) => {
    const ta = extractTime(a), tb = extractTime(b);
    if (ta && tb) return ta - tb;
    if (ta && !tb) return -1;
    if (!ta && tb) return 1;
    return String(a.name || "").localeCompare(String(b.name || ""));
  });

  return items;
}

// ---- Render ----
async function render() {
  const listEl = $("#list");
  const q = $("#q");
  const term = (q?.value || "").trim();

  setMsg("Φόρτωση…");
  listEl.innerHTML = "";

  try {
    const items = await fetchApproved(term);
    setMsg("");
    if (!items.length) {
      listEl.innerHTML = `<div class="empty">Δεν βρέθηκαν εγκεκριμένες παραστάσεις.</div>`;
      return;
    }
    const frag = document.createDocumentFragment();
    items.forEach((p) => frag.appendChild(card(p)));
    listEl.appendChild(frag);
  } catch (e) {
    console.error("fetchApproved error:", e);
    const msg = e?.status === 401 ? "Απαιτείται σύνδεση." : "Σφάλμα φόρτωσης.";
    setMsg(msg, "error");
  }
}

// ---- Bootstrap ----
document.addEventListener("DOMContentLoaded", () => {
  $("#btn-search")?.addEventListener("click", render);
  $("#q")?.addEventListener("keydown", (e) => { if (e.key === "Enter") render(); });
  render();
});
