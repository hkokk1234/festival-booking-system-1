import { http, setToken } from "./api.js";

const CFG = window.APP_CONFIG || {};
const LOGIN = CFG.AUTH?.LOGIN || "/auth/login";
const REGISTER = CFG.AUTH?.REGISTER || "/auth/register";

export function currentUserLabel() {
  // Αν έχεις endpoint "me", μπορείς να το καλέσεις και να εμφανίσεις κανονικά στοιχεία.
  // Εδώ απλά δείχνουμε αν υπάρχει token.
  return localStorage.getItem("access_token") ? "Signed in" : "Not signed in";
}

export async function login(username, password) {
  const res = await http.post(LOGIN, { username, password });
  const token = res?.accessToken || res?.token || res?.jwt || null;
  if (!token) throw new Error("No token returned from server");
  setToken(token);
  return true;
}

export async function register({ username, password, email }) {
  const res = await http.post(REGISTER, { username, password, email });
  // Κάποια APIs επιστρέφουν και token μετά το register. Υποστήριξέ το προαιρετικά:
  const token = res?.accessToken || res?.token || null;
  if (token) setToken(token);
  return res;
}

export function logout() {
  setToken(null);
}
