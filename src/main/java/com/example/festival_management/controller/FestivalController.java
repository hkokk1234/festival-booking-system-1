package com.example.festival_management.controller;

import com.example.festival_management.entity.Enums.FestivalState;
import com.example.festival_management.entity.Festival;
import com.example.festival_management.repository.FestivalRepository;

import jakarta.validation.Valid;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/festivals", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class FestivalController {

    private final FestivalRepository repo;

    public FestivalController(FestivalRepository repo) {
        this.repo = repo;
    }
    // request opws to STELEI o client (enum sto field state)
  public static record CreateFestivalRequest(
    String name,
    String venue,
    LocalDate startDate,
    LocalDate endDate,
    FestivalState state,         // <- το entity enum εδώ
    String description
) {}
    //an erthei state os STRING (optional helper gia symmetry)
    // overload otan to request dinei HDH enum

public FestivalState toStateOrDefault(String s){
  if (s == null || s.isBlank()) return FestivalState.SCHEDULING;
  try { return FestivalState.valueOf(s.trim().toUpperCase()); }
  catch (IllegalArgumentException ex){ return FestivalState.SCHEDULING; }
}
  // Μπορείς να γυρίσεις και DTO – εδώ γυρνάω το entity για συντομία
 @PostMapping
  public ResponseEntity<Festival> create(@Valid @RequestBody CreateFestivalRequest req) {
    if (req.name() == null || req.name().isBlank() ||
        req.venue() == null || req.venue().isBlank() ||
        req.startDate() == null || req.endDate() == null ||
        req.startDate().isAfter(req.endDate())) {
      return ResponseEntity.badRequest().build();
    }

    Festival f = new Festival();
FestivalState f1;
    f.setName(req.name());
    f.setVenue(req.venue());
    f.setStartDate(req.startDate());
    f.setEndDate(req.endDate());
    f.setDescription(req.description());
  f.setState(com.example.festival_management.entity.enums.FestivalState.SCHEDULING); // ✅ πάντα entity.enums.FestivalState

    f = repo.save(f);
    return ResponseEntity.created(URI.create("/api/festivals/" + f.getId())).body(f);
  }
  
    //  pagination + proairetiko q

@GetMapping
public ResponseEntity<?> list(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    try {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        String query = (q == null) ? null : q.trim();
        Page<Festival> result = (query == null || query.isEmpty())
                ? repo.findAll(pageable)
                : repo.search(query, pageable);

        return ResponseEntity.ok(result);
    } catch (IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    } catch (Exception ex) {
        return ResponseEntity.internalServerError().body("Failed to fetch festivals.");
    }
}

 
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return repo.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to fetch festival.");
        }
    }
}
