// src/main/java/com/example/festival_management/repository/PerformanceRepository.java
package com.example.festival_management.repository;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.Performance;
import com.example.festival_management.entity.enums.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.List;
import java.util.Optional;
// Repository gia Festival entities (CRUD + custom queries)
public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    // ΥΠΑΡΧΟΝΤΑ πεδία στο entity: name, festival, status
    boolean existsByNameAndFestival(String name, Festival festival);

    // Για λίστα ανά festivalId (χρησιμοποιείται από controller)
    Page<Performance> findByFestivalId(Long festivalId, Pageable pageable);

    // (προαιρετικό) αναζήτηση στο genre – υπάρχει στο schema
    Page<Performance> findByGenreContainingIgnoreCase(String q, Pageable pageable);

    // Για autoRejectUnsubmittedPerformances(...)
    List<Performance> findByFestivalAndStatus(Festival festival, PerformanceStatus status);

    // Για getApprovedPerformances()
    List<Performance> findByStatus(PerformanceStatus status);

     Page<Performance> findAll(Pageable pageable);



     
  boolean existsByNameIgnoreCaseAndFestivalId(String name, Long festivalId);
@Query("""
  select p from Performance p
  where p.status in :statuses
""")
Page<Performance> findByStatusIn(@Param("statuses") Collection<PerformanceStatus> statuses, Pageable pageable);
   @Query("""
      select p from Performance p
      left join p.festival f
      left join p.mainArtist a
      where p.status in :statuses
        and (
          :term is null or :term = '' or
          lower(p.name) like lower(concat('%', :term, '%')) or
          lower(coalesce(p.genre,'')) like lower(concat('%', :term, '%')) or
          lower(coalesce(f.name,'')) like lower(concat('%', :term, '%')) or
          lower(coalesce(a.username,'')) like lower(concat('%', :term, '%'))
        )
    """)
    
Page<Performance> searchByStatuses(@Param("statuses") Collection<PerformanceStatus> statuses,
                                   @Param("term") String term,
                                   Pageable pageable);
    // 3) Ένα status με Spring method
    Page<Performance> findByStatusAndNameContainingIgnoreCase(
        PerformanceStatus status, String name, Pageable pageable);

    // 4) Ένα status με custom @Query
    @Query("""
      select p from Performance p
      left join p.festival f
      left join p.mainArtist a
      where p.status = :status
        and (
          :term is null or :term = '' or
          lower(p.name) like lower(concat('%', :term, '%')) or
          lower(coalesce(p.genre,'')) like lower(concat('%', :term, '%')) or
          lower(coalesce(f.name,'')) like lower(concat('%', :term, '%')) or
          lower(coalesce(a.username,'')) like lower(concat('%', :term, '%'))
        )
    """)
    Page<Performance> searchByStatus(
        @Param("status") PerformanceStatus status,
        @Param("term") String term,
        Pageable pageable
    );


    
}
