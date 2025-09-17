// Helper πάνω από fetch με Bearer token από localStorage
const CFG = window.APP_CONFIG || {};
const BASE = (CFG.API_BASE || "/api").replace(/\/+$/,"");

function getToken() {
  return localStorage.getItem("access_token") || null;
}
export function setToken(token) {
  if (token) localStorage.setItem("access_token", token);
  else localStorage.removeItem("access_token");
}

export async function httpRequest(path, { method="GET", data, headers={}, params } = {}) {
  // --- SAFE join: μην διπλασιάζεις το BASE ---
  let url;
  if (path.startsWith("http://") || path.startsWith("https://")) {
    url = new URL(path);
  } else if (path.startsWith(BASE + "/")) {
    // Ήδη ξεκινά με /api … κράτα το όπως είναι
    url = new URL(path, window.location.origin);
  } else if (path.startsWith("/")) {
    // Απόλυτο app-path, πρόσθεσε ΜΟΝΟ το origin
    url = new URL(path, window.location.origin);
  } else {
    // Σχετικό προς BASE
    url = new URL(`${BASE}/${path}`, window.location.origin);
  }

  if (params && typeof params === "object") {
    for (const [k, v] of Object.entries(params)) {
      if (v == null || v === "") continue;
      if (Array.isArray(v)) v.forEach(x => url.searchParams.append(k, x));
      else url.searchParams.set(k, v);
    }
  }

  const opts = {
    method,
    headers: { Accept: "application/json", ...headers },
    credentials: "include"
  };

  const token = getToken();
  if (token) opts.headers.Authorization = `Bearer ${token}`;

  if (data !== undefined) {
    if (data instanceof FormData) {
      delete opts.headers["Content-Type"];
      opts.body = data;
    } else {
      opts.headers["Content-Type"] = "application/json";
      opts.body = JSON.stringify(data);
    }
  }

  const res = await fetch(url.toString(), opts);

  // 204/205 → no body
  if (res.status === 204 || res.status === 205) return null;

  const ct = res.headers.get("content-type") || "";
  const isJson = ct.includes("application/json");

  if (!res.ok) {
    const payload = isJson ? await res.json().catch(()=>null) : await res.text().catch(()=>null);
    const err = new Error(`HTTP ${res.status}`);
    err.status = res.status;
    err.payload = payload;
    throw err;
  }

  return isJson ? (await res.json().catch(()=>null)) : (await res.text().catch(()=>null));
}

// /static/js/api.js
export const http = {
  async get(url, { params } = {}) {
    const u = new URL(url, location.origin);
    if (params) Object.entries(params).forEach(([k, v]) => v != null && u.searchParams.set(k, v));
     const headers = { Accept: "application/json" };
  const token = localStorage.getItem("access_token");
    if (token) headers["Authorization"] = "Bearer " + token;

    const res = await fetch(u, { headers, credentials: "include" });
    if (!res.ok) throw Object.assign(new Error(res.statusText), { status: res.status, url: u.toString() });
    const ct = res.headers.get("content-type") || "";
    return ct.includes("application/json") ? res.json() : res.text();
  },
};
