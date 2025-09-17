package com.example.festival_management.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.example.festival_management.entity.enums.PerformanceStatus;
//KLASH ME TA PEDIA THS PARASTASHS
@Entity
@Table(name = "performances",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "festival_id"})})
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceStatus status;

    @Convert(converter = com.example.festival_management.util.DurationToLongConverter.class)
@Column(nullable = false)
private Duration duration;



    
    @ElementCollection
    @CollectionTable(name = "performance_technical_requirements", joinColumns = @JoinColumn(name = "performance_id"))
    @Column(name = "requirement")
    private Set<String> technicalRequirements = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "performance_merchandise", joinColumns = @JoinColumn(name = "performance_id"))
    @Column(name = "item")
    private Set<String> merchandiseItems = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "performance_setlist", joinColumns = @JoinColumn(name = "performance_id"))
    @Column(name = "song")
    private Set<String> setlist = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "performance_rehearsal_slots", joinColumns = @JoinColumn(name = "performance_id"))
    @Column(name = "rehearsal_time")
    private Set<LocalDateTime> preferredRehearsalTimes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "performance_time_slots", joinColumns = @JoinColumn(name = "performance_id"))
    @Column(name = "performance_time")
    private Set<LocalDateTime> preferredPerformanceSlots = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "main_artist_id", nullable = false)
    private User mainArtist;

    @ManyToMany
    @JoinTable(
            name = "performance_band_members",
            joinColumns = @JoinColumn(name = "performance_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> bandMembers = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @OneToOne(mappedBy = "performance", cascade = CascadeType.ALL)
    private Review review;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    public Performance() {}

    // -------- Getters / Setters --------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public PerformanceStatus getStatus() { return status; }
    public void setStatus(PerformanceStatus status) { this.status = status; }

    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }

    public Set<String> getTechnicalRequirements() { return technicalRequirements; }
    public void setTechnicalRequirements(Set<String> technicalRequirements) { this.technicalRequirements = technicalRequirements; }

    public Set<String> getMerchandiseItems() { return merchandiseItems; }
    public void setMerchandiseItems(Set<String> merchandiseItems) { this.merchandiseItems = merchandiseItems; }

    public Set<String> getSetlist() { return setlist; }
    public void setSetlist(Set<String> setlist) { this.setlist = setlist; }

    public Set<LocalDateTime> getPreferredRehearsalTimes() { return preferredRehearsalTimes; }
    public void setPreferredRehearsalTimes(Set<LocalDateTime> preferredRehearsalTimes) { this.preferredRehearsalTimes = preferredRehearsalTimes; }

    public Set<LocalDateTime> getPreferredPerformanceSlots() { return preferredPerformanceSlots; }
    public void setPreferredPerformanceSlots(Set<LocalDateTime> preferredPerformanceSlots) { this.preferredPerformanceSlots = preferredPerformanceSlots; }

    public User getMainArtist() { return mainArtist; }
    public void setMainArtist(User mainArtist) { this.mainArtist = mainArtist; }

    public Set<User> getBandMembers() { return bandMembers; }
    public void setBandMembers(Set<User> bandMembers) { this.bandMembers = bandMembers; }

    public Festival getFestival() { return festival; }
    public void setFestival(Festival festival) { this.festival = festival; }

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }

    public User getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(User assignedStaff) { this.assignedStaff = assignedStaff; }
}
