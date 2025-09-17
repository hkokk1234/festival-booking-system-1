// src/main/java/.../security/CustomUserDetailsService.java
package com.example.festival_management.security;

import com.example.festival_management.entity.RoleAssignment;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.RoleType;
import com.example.festival_management.repository.RoleAssignmentRepository;
import com.example.festival_management.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;
    private final RoleAssignmentRepository roleAssignments;

    public CustomUserDetailsService(UserRepository users, RoleAssignmentRepository roleAssignments) {
        this.users = users;
        this.roleAssignments = roleAssignments;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Πάρε όλους τους ρόλους του χρήστη από τα assignments (σε όλα τα festivals)
        List<RoleAssignment> ras = roleAssignments.findByUser(u);

        Set<String> roleNames = new HashSet<>();
        roleNames.add("ROLE_USER"); // default

        for (RoleAssignment ra : ras) {
            RoleType rt = ra.getRole();
            if (rt == null) continue;
            switch (rt) {
                case ADMIN      -> roleNames.add("ROLE_ADMIN");
                case ORGANIZER  -> roleNames.add("ROLE_ORGANIZER");
                case STAFF      -> roleNames.add("ROLE_STAFF");
                case ARTIST     -> roleNames.add("ROLE_ARTIST");
                default         -> roleNames.add("ROLE_USER");
            }
        }

        List<GrantedAuthority> auths = roleNames.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                auths
        );
    }
}
