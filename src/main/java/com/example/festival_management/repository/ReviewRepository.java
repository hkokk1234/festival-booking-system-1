package com.example.festival_management.repository;

import com.example.festival_management.entity.Performance;
import com.example.festival_management.entity.Review;
import com.example.festival_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
//METHODOI GIA LEITOURGIES PROS TA PERFORMANCES
    Optional<Review> findByPerformance(Performance performance);

    Optional<Review> findByReviewerAndPerformance(User reviewer, Performance performance);

    boolean existsByPerformance(Performance performance);
}
