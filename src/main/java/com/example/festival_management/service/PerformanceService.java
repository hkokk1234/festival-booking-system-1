package com.example.festival_management.service;

import com.example.festival_management.entity.Performance;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PerformanceService {

    Performance createPerformance(Performance performance, Long festivalId, User creator);

    Performance updatePerformance(Long performanceId, Performance updatedPerformance, User artist);

    void deletePerformance(Long performanceId, User artist);

    Performance submitPerformance(Long performanceId, User artist);

    Performance finalSubmitPerformance(Long performanceId, User artist);

    Performance approvePerformance(Long performanceId, User organizer);

    Performance rejectPerformance(Long performanceId, String reason, User organizer);

    void autoRejectUnsubmittedPerformances(Festival festival);

    Performance assignStaffToPerformance(Long performanceId, User staff, User organizer);

    Performance reviewPerformance(Long performanceId, User staffReviewer, int score, String comments);
    Performance acceptPerformance(Long performanceId, User organizer);

    Performance getPerformanceById(Long performanceId, User requestingUser);

    List<Performance> searchPerformances(String name, String genre, String artistName, User requestingUser);

Page<Performance> getApproved(String status, String q, Pageable pageable);

List<Performance> getApprovedPerformances();


}
