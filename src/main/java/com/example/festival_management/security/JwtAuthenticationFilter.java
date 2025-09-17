package com.example.festival_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.List;

//@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

    String path = request.getServletPath();
    if (path.startsWith("/assets/") || path.startsWith("/css/") || path.startsWith("/js/")
        || path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css")
        || path.equals("/") || path.equals("/favicon.ico") || path.startsWith("/h2-console/")) {
      chain.doFilter(request, response);
      return;
    }

    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      // δεν υπάρχει token -> μην ρίχνεις exception, απλά προχώρα
      chain.doFilter(request, response);
      return;
    }

    // ... κανονικός έλεγχος JWT εδώ ...
    chain.doFilter(request, response);
  }
}
