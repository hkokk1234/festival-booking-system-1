package com.example.festival_management.service.impl;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.Performance;
import com.example.festival_management.entity.Review;
import com.example.festival_management.entity.RoleAssignment;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.FestivalState;
import com.example.festival_management.entity.enums.PerformanceStatus;
import com.example.festival_management.entity.enums.RoleType;
import com.example.festival_management.repository.FestivalRepository;
import com.example.festival_management.repository.PerformanceRepository;
import com.example.festival_management.repository.ReviewRepository;
import com.example.festival_management.repository.RoleAssignmentRepository;
import com.example.festival_management.service.PerformanceService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
 // Ylopoiisi performanceservices me kanones asfaleias/epixirisiakis logikis

@Service
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final FestivalRepository festivalRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final ReviewRepository reviewRepository;

    public PerformanceServiceImpl(PerformanceRepository performanceRepository,
                              FestivalRepository festivalRepository,
                              RoleAssignmentRepository roleAssignmentRepository,
                              ReviewRepository reviewRepository) {
    this.performanceRepository = performanceRepository;
    this.festivalRepository = festivalRepository;
    this.roleAssignmentRepository = roleAssignmentRepository;
    this.reviewRepository = reviewRepository;
}
  // repositories pou xreiazontai
//methodoi kai energeies pou aforoun perforamnce
@Override
@Transactional
public Performance createPerformance(Performance performance, Long festivalId, User creator) {
    var festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new NoSuchElementException("Festival not found"));

    if (performanceRepository.existsByNameAndFestival(performance.getName(), festival)) {
        throw new IllegalArgumentException("Performance name already exists in this festival");
    }

    // Φέρε τον creator από τη ΒΔ με βάση username (όχι transient αντικείμενο)
    // Αν έχεις UserRepository:
    // var dbCreator = userRepository.findByUsername(creator.getUsername())
    //         .orElseThrow(() -> new NoSuchElementException("User not found: " + creator.getUsername()));
    // Αν δεν έχεις ακόμη UserRepository, κάνε inject και πρόσθεσε method findByUsername(String).

    performance.setCreatedAt(LocalDateTime.now());
    performance.setFestival(festival);
    performance.setMainArtist(/* dbCreator */ creator); // <-- βάλ’ τον από τη ΒΔ!
    performance.setStatus(PerformanceStatus.CREATED);
    performance.getBandMembers().add(/* dbCreator */ creator);

    var saved = performanceRepository.save(performance);

    boolean hasArtistRole =
            roleAssignmentRepository.existsByUserAndFestivalAndRole(/* dbCreator */ creator, festival, RoleType.ARTIST);
    if (!hasArtistRole) {
        var role = new RoleAssignment();
        role.setUser(/* dbCreator */ creator);
        role.setFestival(festival);
        role.setRole(RoleType.ARTIST);
        roleAssignmentRepository.save(role);
    }
    return saved;
}


    @Override
    @Transactional
    public Performance updatePerformance(Long performanceId, Performance updated, User artist) {
        Performance existing = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));

        if (!Objects.equals(existing.getMainArtist(), artist)) {
            throw new SecurityException("Only the main artist can update the performance");
        }

        if (existing.getStatus() != PerformanceStatus.CREATED) {
            throw new IllegalStateException("Cannot update after submission");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setGenre(updated.getGenre());
        existing.setDuration(updated.getDuration());
        existing.setSetlist(updated.getSetlist());
        existing.setTechnicalRequirements(updated.getTechnicalRequirements());
        existing.setMerchandiseItems(updated.getMerchandiseItems());
        existing.setPreferredRehearsalTimes(updated.getPreferredRehearsalTimes());
        existing.setPreferredPerformanceSlots(updated.getPreferredPerformanceSlots());

        return performanceRepository.save(existing);
    }

    @Override
    @Transactional
    public void deletePerformance(Long performanceId, User artist) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));

        if (!Objects.equals(performance.getMainArtist(), artist)) {
            throw new SecurityException("Only the main artist can delete the performance");
        }

        if (performance.getStatus() != PerformanceStatus.CREATED) {
            throw new IllegalStateException("Only unsubmitted performances can be deleted");
        }

        performanceRepository.delete(performance);
    }

    @Override
    @Transactional
    public Performance submitPerformance(Long performanceId, User artist) {
        Performance performance = getPerformanceIfAuthorized(performanceId, artist);
        Festival festival = performance.getFestival();

        if (festival.getState() != FestivalState.SUBMISSION) {
            throw new IllegalStateException("Festival is not in SUBMISSION state");
        }

        validatePerformanceCompleteness(performance);

        performance.setStatus(PerformanceStatus.SUBMITTED);
        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public Performance finalSubmitPerformance(Long performanceId, User artist) {
        Performance performance = getPerformanceIfAuthorized(performanceId, artist);

        if (performance.getStatus() != PerformanceStatus.APPROVED) {
            throw new IllegalStateException("Only approved performances can be finally submitted");
        }

        Festival festival = performance.getFestival();
        if (festival.getState() != FestivalState.FINAL_SUBMISSION) {
            throw new IllegalStateException("Festival is not in FINAL_SUBMISSION state");
        }

        performance.setStatus(PerformanceStatus.FINAL_SUBMITTED);
        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public Performance approvePerformance(Long performanceId, User organizer) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));

        Festival festival = performance.getFestival();

        if (!roleAssignmentRepository.existsByUserAndFestivalAndRole(organizer, festival, RoleType.ORGANIZER)) {
            throw new SecurityException("Only organizers can approve performances");
        }

        if (festival.getState() != FestivalState.SCHEDULING) {
            throw new IllegalStateException("Festival must be in SCHEDULING state");
        }

        performance.setStatus(PerformanceStatus.APPROVED);
        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public Performance rejectPerformance(Long performanceId, String reason, User organizer) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));

        Festival festival = performance.getFestival();

        if (!roleAssignmentRepository.existsByUserAndFestivalAndRole(organizer, festival, RoleType.ORGANIZER)) {
            throw new SecurityException("Only organizers can reject performances");
        }

        if (festival.getState() != FestivalState.SCHEDULING && festival.getState() != FestivalState.DECISION) {
            throw new IllegalStateException("Rejection only allowed in SCHEDULING or DECISION");
        }

        performance.setStatus(PerformanceStatus.REJECTED);
        // TODO: αποθήκευσε κάπου το reason (π.χ. πεδίο notes) αν το προσθέσεις στο entity
        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public void autoRejectUnsubmittedPerformances(Festival festival) {
        List<Performance> approved = performanceRepository.findByFestivalAndStatus(festival, PerformanceStatus.APPROVED);
        for (Performance p : approved) {
            p.setStatus(PerformanceStatus.REJECTED);
            performanceRepository.save(p);
        }
    }

    @Override
    @Transactional
    public Performance assignStaffToPerformance(Long performanceId, User staff, User organizer) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));
        Festival festival = performance.getFestival();

        if (!roleAssignmentRepository.existsByUserAndFestivalAndRole(organizer, festival, RoleType.ORGANIZER)) {
            throw new SecurityException("Only organizers can assign staff");
        }

        if (!roleAssignmentRepository.existsByUserAndFestivalAndRole(staff, festival, RoleType.STAFF)) {
            throw new IllegalArgumentException("User is not registered as STAFF for this festival");
        }

        if (festival.getState() != FestivalState.ASSIGNMENT) {
            throw new IllegalStateException("Festival is not in ASSIGNMENT state");
        }

        performance.setAssignedStaff(staff);
        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public Performance acceptPerformance(Long performanceId, User organizer) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));
        Festival festival = performance.getFestival();

        if (!roleAssignmentRepository.existsByUserAndFestivalAndRole(organizer, festival, RoleType.ORGANIZER)) {
            throw new SecurityException("Only organizers can accept performances");
        }

        if (festival.getState() != FestivalState.DECISION) {
            throw new IllegalStateException("Festival is not in DECISION state");
        }

        performance.setStatus(PerformanceStatus.ACCEPTED);
        return performanceRepository.save(performance);
    }

    @Override
    public Performance getPerformanceById(Long performanceId, User requestingUser) {
        return performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));
        // TODO: role-based projection αν χρειάζεται
    }

   @Override
public List<Performance> getApprovedPerformances() {
    return performanceRepository.findByStatus(PerformanceStatus.APPROVED);
}

    @Override
    public List<Performance> searchPerformances(String name, String genre, String artistName, User user) {
        // TODO: φίλτρα/specific queries
        return performanceRepository.findAll();
    }

    // ---------- Helpers ----------

    private Performance getPerformanceIfAuthorized(Long performanceId, User artist) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new NoSuchElementException("Performance not found"));
        if (!Objects.equals(p.getMainArtist(), artist)) {
            throw new SecurityException("You are not authorized to modify this performance");
        }
        return p;
    }

    private void validatePerformanceCompleteness(Performance p) {
        if (p.getName() == null || p.getName().isBlank()
                || p.getDescription() == null || p.getDescription().isBlank()
                || p.getGenre() == null || p.getGenre().isBlank()
                || p.getDuration() == null
                || p.getBandMembers() == null || p.getBandMembers().isEmpty()
                || p.getTechnicalRequirements() == null || p.getTechnicalRequirements().isEmpty()
                || p.getSetlist() == null || p.getSetlist().isEmpty()
                || p.getMerchandiseItems() == null || p.getMerchandiseItems().isEmpty()
                || p.getPreferredPerformanceSlots() == null || p.getPreferredPerformanceSlots().isEmpty()
                || p.getPreferredRehearsalTimes() == null || p.getPreferredRehearsalTimes().isEmpty()) {
            throw new IllegalStateException("Performance details are incomplete");
        }
    }


    @Override
public Page<Performance> getApproved(String status, String q, Pageable pageable) {
    // Αν ο client περάσει ?status=..., σεβόμαστε ακριβώς αυτό.
    // Αλλιώς, default: όλα όσα θεωρούνται έτοιμα για εμφάνιση στο κοινό.
    List<PerformanceStatus> statuses;

    if (status != null && !status.isBlank()) {
        try {
            statuses = List.of(PerformanceStatus.valueOf(status.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            // Άκυρη τιμή -> πέφτουμε στο default
            statuses= List.of(
                PerformanceStatus.APPROVED,
                PerformanceStatus.ACCEPTED,
                PerformanceStatus.FINAL_SUBMITTED
                // αν θέλεις, πρόσθεσε και PerformanceStatus.CONFIRMED
            );
        }
    } else {
        statuses = List.of(
            PerformanceStatus.APPROVED,
            PerformanceStatus.ACCEPTED,
            PerformanceStatus.FINAL_SUBMITTED
            // αν θέλεις, πρόσθεσε και PerformanceStatus.CONFIRMED
        );
    }

    return (q != null && !q.isBlank())
            ? performanceRepository.searchByStatuses(statuses, q, pageable)
            : performanceRepository.findByStatusIn(statuses, pageable);
}

@Override
@Transactional
public Performance reviewPerformance(Long performanceId, User staffReviewer, int score, String comments) {
    Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new NoSuchElementException("Performance not found"));
    Festival festival = performance.getFestival();

    // Αρκεί να είναι STAFF στο ίδιο festival (όχι απαραίτητα assigned, ούτε συγκεκριμένο festival state)
    boolean isStaffOfFestival =
            roleAssignmentRepository.existsByUserAndFestivalAndRole(staffReviewer, festival, RoleType.STAFF);
    if (!isStaffOfFestival) {
        throw new SecurityException("Only STAFF of this festival can review performances");
    }

    // Upsert (ένα review ανά performance – αν υπάρχει, ενημέρωσέ το)
    Review review = reviewRepository.findByPerformance(performance).orElse(new Review());
    review.setPerformance(performance);
    review.setReviewer(staffReviewer);
    review.setScore(score);
    review.setComments(comments);

    reviewRepository.save(review);
    performance.setReview(review);
    return performanceRepository.save(performance);
}

    // Αν θέλεις να δουλέψει και η (ήδη δηλωμένη) λίστα:
   
}
