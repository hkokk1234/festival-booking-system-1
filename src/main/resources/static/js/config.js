// Ρυθμίσεις runtime (άλλαξέ τες ανά περιβάλλον)
window.APP_CONFIG = {
  API_BASE: "/api",       // same-origin πίσω από Spring Boot
  AUTH: {
    LOGIN: "/auth/login",
    REGISTER: "/auth/register"
  },
  RESOURCES: {
    PERFORMANCES: "/performances/APPROVED", // GET (και ό,τι άλλο υποστηρίζει το API σου)
USERS: "/users"  // GET (και ό,τι άλλο υποστηρίζει το API σου)
    
  }
};
