// src/main/java/com/example/festival_management/security/JwtFilter.java
package com.example.festival_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// ★★ ΑΥΤΑ ΕΛΕΙΠΑΝ ★★
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// ★★ ---------------- ★★

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

   private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getServletPath();

        if (HttpMethod.OPTIONS.matches(method)) return true;
        if (path.startsWith("/h2-console")) return true;

        // static assets
        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) return true;
        if (path.startsWith("/assets/") || path.startsWith("/css/") ||
            path.startsWith("/js/") || path.startsWith("/images/") ||
            path.startsWith("/static/") || path.startsWith("/webjars/")) return true;

        // αφήνω login/register χωρίς φίλτρο (public)
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) return true;

        // Όλα τα υπόλοιπα θα περνάνε από το φίλτρο και αν υπάρχει JWT θα γίνει auth
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7).trim();
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var ud = userDetailsService.loadUserByUsername(username);

                        var auth = new UsernamePasswordAuthenticationToken(
                                ud, null, ud.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
            chain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
