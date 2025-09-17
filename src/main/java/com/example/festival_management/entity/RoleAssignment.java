package com.example.festival_management.entity;

import com.example.festival_management.entity.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(
    name = "role_assignments",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "festival_id", "role"})
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // ταιριάζει με @JsonManagedReference στο User.roleAssignments
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    @JsonBackReference(value = "festival-roleAssignments") // ταιριάζει με @JsonManagedReference(value="festival-roleAssignments") στο Festival
    private Festival festival;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    public RoleAssignment() {}

    public RoleAssignment(User user, Festival festival, RoleType role) {
        this.user = user;
        this.festival = festival;
        this.role = role;
    }

    // -------- getters / setters --------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Festival getFestival() { return festival; }
    public void setFestival(Festival festival) { this.festival = festival; }

    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }

    // equals/hashCode με βάση το id (ασφαλές για JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleAssignment)) return false;
        RoleAssignment that = (RoleAssignment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return 31; }
}
