package com.example.festival_management.service.impl;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.RoleAssignment;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.FestivalState;
import com.example.festival_management.entity.enums.RoleType;
import com.example.festival_management.repository.FestivalRepository;
import com.example.festival_management.repository.RoleAssignmentRepository;
import com.example.festival_management.service.FestivalService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
 // Ylopoiisi FestivalService me kanones asfaleias/epixirisiakis logikis

@Service
public class FestivalServiceImpl implements FestivalService {

    private static final String FESTIVAL_NOT_FOUND_MSG = "Festival not found";

    private final FestivalRepository festivalRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    public FestivalServiceImpl(FestivalRepository festivalRepository,
                               RoleAssignmentRepository roleAssignmentRepository) {
        this.festivalRepository = festivalRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }
    // repositories pou xreiazontai
//methodoi kai energeies pou aforoun festival
    @Override
    @Transactional
    public Festival createFestival(Festival festival, User creator) {
        if (festivalRepository.existsByName(festival.getName())) {
            throw new IllegalArgumentException("Festival with this name already exists.");
        }

        festival.setState(FestivalState.CREATED);
        festival.setCreatedAt(LocalDate.now());

        Festival saved = festivalRepository.save(festival);

        RoleAssignment role = new RoleAssignment();
        role.setUser(creator);
        role.setFestival(saved);
        role.setRole(RoleType.ORGANIZER);
        roleAssignmentRepository.save(role);

        return saved;
    }

    @Override
    @Transactional
    public Festival updateFestival(Long id, Festival updatedFestival, User organizer) {
        Festival existing = festivalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(FESTIVAL_NOT_FOUND_MSG));

        if (!userIsOrganizer(organizer, existing)) {
            throw new SecurityException("You are not an organizer of this festival.");
        }
        if (existing.getState() == FestivalState.ANNOUNCED) {
            throw new IllegalStateException("Cannot update an announced festival.");
        }

        existing.setName(updatedFestival.getName());
        existing.setDescription(updatedFestival.getDescription());
        existing.setStartDate(updatedFestival.getStartDate());
        existing.setEndDate(updatedFestival.getEndDate());
        existing.setVenue(updatedFestival.getVenue());

        return festivalRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteFestival(Long id, User organizer) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(FESTIVAL_NOT_FOUND_MSG));

        if (!userIsOrganizer(organizer, festival)) {
            throw new SecurityException("You are not an organizer of this festival.");
        }
        if (festival.getState() != FestivalState.CREATED) {
            throw new IllegalStateException("Only CREATED festivals can be deleted.");
        }

        festivalRepository.delete(festival);
    }

    @Override
    public Optional<Festival> getFestivalById(Long id) {
        return festivalRepository.findById(id);
    }

    @Override
    public List<Festival> searchFestivals(String name, String description, String venue, String dates, User requester) {
        return festivalRepository.findAll();
    }

    /** Προαιρετική βοήθεια για UI – δεν είναι απαραίτητα στο interface. */
    public List<Festival> getFestivalsForOrganizer(User organizer) {
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUser(organizer);
        List<Festival> festivals = new ArrayList<>();
        for (RoleAssignment assignment : assignments) {
            if (assignment.getRole() == RoleType.ORGANIZER) {
                festivals.add(assignment.getFestival());
            }
        }
        return festivals;
    }

    @Override
    @Transactional
    public void addOrganizers(Long festivalId, List<User> users, User requestingOrganizer) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new NoSuchElementException(FESTIVAL_NOT_FOUND_MSG));

        if (!userIsOrganizer(requestingOrganizer, festival)) {
            throw new SecurityException("Only organizers can add other organizers.");
        }

        for (User user : users) {
            boolean alreadyAssigned = roleAssignmentRepository
                    .existsByUserAndFestivalAndRole(user, festival, RoleType.ORGANIZER);
            if (!alreadyAssigned) {
                RoleAssignment assignment = new RoleAssignment();
                assignment.setUser(user);
                assignment.setFestival(festival);
                assignment.setRole(RoleType.ORGANIZER);
                roleAssignmentRepository.save(assignment);
            }
        }
    }

    @Override
    @Transactional
    public void addStaff(Long festivalId, List<User> users, User requestingOrganizer) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new NoSuchElementException(FESTIVAL_NOT_FOUND_MSG));

        if (!userIsOrganizer(requestingOrganizer, festival)) {
            throw new SecurityException("Only organizers can add staff.");
        }

        for (User user : users) {
            boolean alreadyAssigned = roleAssignmentRepository
                    .existsByUserAndFestivalAndRole(user, festival, RoleType.STAFF);
            boolean hasConflict = roleAssignmentRepository.existsByUserAndFestival(user, festival)
                    && !userIsStaff(user, festival);

            if (!alreadyAssigned && !hasConflict) {
                RoleAssignment assignment = new RoleAssignment();
                assignment.setUser(user);
                assignment.setFestival(festival);
                assignment.setRole(RoleType.STAFF);
                roleAssignmentRepository.save(assignment);
            }
        }
    }

    @Override
    @Transactional
    public void changeFestivalState(Long festivalId, FestivalState newState, User requestingOrganizer) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new NoSuchElementException(FESTIVAL_NOT_FOUND_MSG));

        if (!userIsOrganizer(requestingOrganizer, festival)) {
            throw new SecurityException("Only organizers can change festival state.");
        }

        festival.setState(newState);
        festivalRepository.save(festival);
    }

    // --------- ΝΕΕΣ ΥΛΟΠΟΙΗΣΕΙΣ για τα GET endpoints ---------
    @Override
    public List<Festival> findAll() {
        return festivalRepository.findAll();
    }

    @Override
    public List<FestivalRepository.Option> options() {
        return festivalRepository.options();
    }
    // -----------------------------------------------------------

    public boolean userIsOrganizer(User user, Festival festival) {
        return roleAssignmentRepository.existsByUserAndFestivalAndRole(user, festival, RoleType.ORGANIZER);
    }

    public boolean userIsStaff(User user, Festival festival) {
        return roleAssignmentRepository.existsByUserAndFestivalAndRole(user, festival, RoleType.STAFF);
    }
}
