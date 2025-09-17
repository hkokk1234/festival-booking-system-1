package com.example.festival_management.entity;

import java.time.LocalDate;

import com.example.festival_management.entity.enums.FestivalState;
import jakarta.persistence.*;
//KLASH ME TA PEDIA FESTIVAL
@Entity
@Table(name = "festivals")
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 2000)
    private String description;

    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FestivalState state = FestivalState.CREATED;

    // Ημερομηνίες
    @Column
    private LocalDate createdAt;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    public Festival() {}

    // ---------- Getters / Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public FestivalState getState() { return state; }
    public void setState(FestivalState state) { this.state = state; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
