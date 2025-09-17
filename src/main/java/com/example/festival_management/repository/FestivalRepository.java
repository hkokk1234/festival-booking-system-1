package com.example.festival_management.repository;

import com.example.festival_management.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
// Repository gia Festival entities (CRUD + custom queries)

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @Query(
        value = """
                SELECT f FROM Festival f
                WHERE (:q IS NULL OR :q = '' OR
                       LOWER(f.name)        LIKE LOWER(CONCAT('%', :q, '%')) OR
                       LOWER(f.description) LIKE LOWER(CONCAT('%', :q, '%')) OR
                       LOWER(f.venue)       LIKE LOWER(CONCAT('%', :q, '%')))
                """,
        countQuery = """
                SELECT COUNT(f) FROM Festival f
                WHERE (:q IS NULL OR :q = '' OR
                       LOWER(f.name)        LIKE LOWER(CONCAT('%', :q, '%')) OR
                       LOWER(f.description) LIKE LOWER(CONCAT('%', :q, '%')) OR
                       LOWER(f.venue)       LIKE LOWER(CONCAT('%', :q, '%')))
                """
    )
    //METHODOI GIA ENERGEIES PROS TO FESTIVAL
    Page<Festival> search(@Param("q") String q, Pageable pageable);

    boolean existsByName(String name);

    Optional<Festival> findByName(String name);

    List<Festival> findByNameContainingIgnoreCase(String namePart);

    List<Festival> findByDescriptionContainingIgnoreCase(String descriptionPart);

    List<Festival> findByVenueContainingIgnoreCase(String venuePart);

    // Projection για combobox (id, name)
    interface Option {
        Long getId();
        String getName();
    }

    @Query("SELECT f.id AS id, f.name AS name FROM Festival f ORDER BY f.name ASC")
    List<Option> options();
}
