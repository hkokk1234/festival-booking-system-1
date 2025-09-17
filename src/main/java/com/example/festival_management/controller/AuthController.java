// com.example.festival_management.controller.AuthController
package com.example.festival_management.controller;
import com.example.festival_management.repository.RoleAssignmentRepository;
import com.example.festival_management.entity.enums.RoleType;
import com.example.festival_management.entity.RoleAssignment;

import com.example.festival_management.entity.User;
import com.example.festival_management.repository.UserRepository;
import com.example.festival_management.security.JwtUtil;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // eksartiseis gia auth/JWT/users/passwords

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
        //  an den yparxei Authentication, epestrepse 401

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No auth"));
        }
        // vrese user me to username pou ebale o JwtFilter sto SecurityContext
        User u = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        //  pare roles apo authorities (minimo ROLE_USER an adeio)

        List<String> roles = auth.getAuthorities() == null
                ? List.of("ROLE_USER")
                : auth.getAuthorities().stream()
                      .map(GrantedAuthority::getAuthority)
                      .distinct()
                      .toList();

        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail(),
                "roles", roles
        ));
    }
        //  aplh login me AuthenticationManager -> JWT + roles

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            String jwt = jwtUtil.generateToken(auth.getName());
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // ΠΑΝΤΑ δώσε ΚΑΙ accessToken ΚΑΙ token
            return ResponseEntity.ok(Map.of(
                    "accessToken", jwt,
                    "token", jwt,
                    "roles", roles
            ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error","Invalid credentials"));
        }
    }

   @PostMapping(path="/register", consumes="application/json", produces="application/json")
public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    if (req.getUsername()==null || req.getUsername().isBlank()
     || req.getPassword()==null || req.getPassword().isBlank()) {
        return ResponseEntity.badRequest().body(Map.of("error","Username and password are required"));
    }
    if (userRepository.existsByUsername(req.getUsername())) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error","Username already exists"));
    }
    if (req.getEmail()!=null && !req.getEmail().isBlank()
        && userRepository.existsByEmail(req.getEmail())) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error","Email already exists"));
    }

    User u = new User();
    u.setUsername(req.getUsername());
    // προσοχή: default email μπορεί να κάνει collisions
    String email = (req.getEmail()!=null && !req.getEmail().isBlank())
            ? req.getEmail()
            : req.getUsername()+"@local";
    u.setEmail(email);
    u.setPassword(passwordEncoder.encode(req.getPassword()));

    try {
        userRepository.save(u);
    } catch (DataIntegrityViolationException ex) {
        // π.χ. αν έγινε race condition και δημιουργήθηκε ίδιο email/username
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error","Username or email already exists"));
    }

    // εδώ δίνεις τον ρόλο ARTIST αν θες
    // roleAssignmentRepository.save(...)

    String token = jwtUtil.generateToken(req.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthResponse(token, new String[]{"ROLE_ARTIST"}));
}
    // ---------- DTOs (χωρίς Lombok) ----------
    public static class LoginRequest {
        private String username;
        private String password;
        public LoginRequest() {}
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    //  enopoihmenh apantisi login/register (accessToken + token + roles)

    public static class RegisterRequest {
        private String username;
        private String password;
        private String email; // optional
        private String role;  // optional (π.χ. ARTIST)
        public RegisterRequest() {}
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

        // enopoihmenh apantisi login/register (accessToken + token + roles)

    public static class AuthResponse {
        private String accessToken;
        private String token; // ίδιο με accessToken
        private String[] roles;

        public AuthResponse() {}
        public AuthResponse(String token, String[] roles) {
            this.accessToken = token;
            this.token = token;
            this.roles = roles != null ? roles : new String[0];
        }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; this.token = accessToken; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; this.accessToken = token; }
        public String[] getRoles() { return roles; }
        public void setRoles(String[] roles) { this.roles = roles; }
    }

    // ---------- helpers ----------
    private static String asString(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }
}
