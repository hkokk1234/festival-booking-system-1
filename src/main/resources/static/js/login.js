const $ = s => document.querySelector(s);
const setMsg = (t, kind) => { const m=$("#msg"); m.textContent=t||""; m.className="msg"+(kind?" "+kind:""); };

// Δοκίμασε πρώτα /auth/login, αλλιώς /api/auth/login
async function doLogin(username, password) {
  const body = JSON.stringify({ username, password });
  const headers = { "Content-Type": "application/json" };

  for (const url of ["/auth/login", "/api/auth/login"]) {
    try {
      const res = await fetch(url, { method:"POST", headers, body });
      if (!res.ok) continue;
      const data = await res.json();
      const token = data.token || data.jwt || data.accessToken;
      if (token) return token;
    } catch(_) {}
  }
  throw new Error("Login failed");
}

document.addEventListener("DOMContentLoaded", () => {
  $("#btn").addEventListener("click", async () => {
    setMsg("Σύνδεση…");
    try {
      const token = await doLogin($("#u").value.trim(), $("#p").value);
      localStorage.setItem("jwt", token);
      setMsg("OK! Μεταφορά…");
      location.href = "/performances1.html";
    } catch (e) {
      setMsg("Λάθος στοιχεία ή endpoint.", "error");
    }
  });
});
