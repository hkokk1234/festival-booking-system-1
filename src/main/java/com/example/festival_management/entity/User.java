package com.example.festival_management.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Να μην εμφανίζεται στο JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    // Προαιρετικό — μόνο αν θες findByResetToken. Κρυμμένο στο JSON.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "reset_token")
    private String resetToken;

    // Ένα και μοναδικό mapping προς RoleAssignment (όχι διπλό πεδίο)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<RoleAssignment> roleAssignments = new HashSet<>();

    public User() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Set<RoleAssignment> getRoleAssignments() { return roleAssignments; }
    public void setRoleAssignments(Set<RoleAssignment> roleAssignments) { this.roleAssignments = roleAssignments; }

    // Βοηθητικά για να κρατάς και τις δύο πλευρές συγχρονισμένες
    public void addRoleAssignment(RoleAssignment assignment) {
        roleAssignments.add(assignment);
        assignment.setUser(this);
    }

    public void removeRoleAssignment(RoleAssignment assignment) {
        roleAssignments.remove(assignment);
        assignment.setUser(null);
    }
    
}
