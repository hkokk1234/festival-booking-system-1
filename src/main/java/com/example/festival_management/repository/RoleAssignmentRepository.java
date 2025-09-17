package com.example.festival_management.repository;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.RoleAssignment;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.RoleType;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
//  Repository gia role entities (CRUD + custom queries)

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
//ENERGIES POU AFOROUN ROLES XRHSTWN
    Optional<RoleAssignment> findByUserAndFestival(User user, Festival festival);

    List<RoleAssignment> findByUser(User user);

    List<RoleAssignment> findByFestival(Festival festival);

    List<RoleAssignment> findByFestivalAndRole(Festival festival, RoleType role);

    boolean existsByUserAndFestivalAndRole(User user, Festival festival, RoleType role);

    boolean existsByUserAndFestival(User user, Festival festival);

    @Query("""
  select distinct u
  from User u
  left join fetch u.roleAssignments ra
  left join fetch ra.festival
""")
List<User> findAllWithRolesAndAssignments();




}
