package com.example.festival_management.repository;

import com.example.festival_management.entity.RoleAssignment;
import com.example.festival_management.entity.User;

import jakarta.persistence.OneToMany;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.HashSet;
import java.util.List;

// greeklish: Repository gia USER entities (CRUD + custom queries)

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Κράτησέ το μόνο αν υπάρχει πεδίο resetToken στο User entity
    Optional<User> findByResetToken(String resetToken);
    // Φέρε τους users μαζί με roles/assignments σε ένα query
    @Query("""
        select distinct u
        from User u
        left join fetch u.roleAssignments ra
        left join fetch ra.festival f
    """)
    List<User> findAllWithRolesAndAssignments();

}
