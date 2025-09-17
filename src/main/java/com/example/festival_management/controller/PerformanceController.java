// src/main/java/com/example/festival_management/controller/PerformanceController.java
package com.example.festival_management.controller;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.Performance;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.PerformanceStatus;
import com.example.festival_management.repository.FestivalRepository;
import com.example.festival_management.repository.PerformanceRepository;
import com.example.festival_management.repository.UserRepository;
import com.example.festival_management.service.PerformanceService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/performances")
@CrossOrigin(origins = "*")
public class PerformanceController {

    private final PerformanceRepository performanceRepo;
    private final FestivalRepository festivalRepo;
    private final UserRepository userRepo;
    private final PerformanceService performanceService;

    public PerformanceController(PerformanceRepository performanceRepo,
                                 FestivalRepository festivalRepo,
                                 UserRepository userRepo,
                                 PerformanceService performanceService) {
        this.performanceRepo = performanceRepo;
        this.festivalRepo = festivalRepo;
        this.userRepo = userRepo;
        this.performanceService = performanceService;
    }

    // ========= HELPERS =========
    private static String str(Object o){ return o==null?null:String.valueOf(o); }
    private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
    private static Long toLong(Object o){
        if (o==null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch(Exception e){ return null; }
    }
    private static String mostSpecific(Throwable t){
        return (t.getCause()!=null) ? mostSpecific(t.getCause()) : (t.getMessage()==null?"":t.getMessage());
    }
    private static ResponseEntity<Map<String,Object>> bad(String msg){
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }
    @PostMapping("/{performanceId}/approve")
public ResponseEntity<?> approve(
        @PathVariable Long performanceId,
        Authentication authentication
){
    if (authentication == null || authentication.getName() == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","No auth"));
    }
    var organizer = userRepo.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    var perf = performanceService.approvePerformance(performanceId, organizer);
    return ResponseEntity.ok(Map.of(
            "ok", true,
            "performanceId", perf.getId(),
            "status", perf.getStatus().name()
    ));
}

@PostMapping(
  value = "/{performanceId}/reject",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<?> reject(
        @PathVariable Long performanceId,
        @RequestBody Map<String,Object> body,
        Authentication authentication
){
    if (authentication == null || authentication.getName() == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","No auth"));
    }
    var organizer = userRepo.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    String reason = Optional.ofNullable(body.get("reason")).map(String::valueOf).orElse("").trim();
    if (reason.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","Reason is required"));

    var perf = performanceService.rejectPerformance(performanceId, reason, organizer);
    return ResponseEntity.ok(Map.of(
            "ok", true,
            "performanceId", perf.getId(),
            "status", perf.getStatus().name(),
            "reason", reason
    ));
}

    private PerformanceStatus parseStatus(Object v){
        if (v==null) return PerformanceStatus.SUBMITTED; // default
        String s = String.valueOf(v).trim().toUpperCase(Locale.ROOT);
        try { return PerformanceStatus.valueOf(s); }
        catch (Exception ignore){ return PerformanceStatus.SUBMITTED; }
    }
    private Duration parseDuration(Map<String,Object> body){
        // ΔΕΧΕΤΑΙ: durationMinutes (min) Ή duration (sec)
        Long mins = toLong(body.get("durationMinutes"));
        Long sec  = toLong(body.get("duration"));
        if (mins!=null && mins>0) return Duration.ofMinutes(mins);
        if (sec !=null && sec >0) return Duration.ofSeconds(sec);
        return null;
    }
    private User resolveMainArtist(Map<String,Object> body, Authentication authentication){
        // 1) mainArtistId από το σώμα
        Long id = toLong(body.get("mainArtistId"));
        if (id!=null){
            return userRepo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "mainArtistId not found: "+id));
        }
        // 2) Από JWT (Authentication)
        if (authentication!=null && !isBlank(authentication.getName())){
            return userRepo.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user not found: "+authentication.getName()));
        }
        // 3) Τελικό fallback -> 400
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing mainArtist (send mainArtistId or authenticate)");
    }

    private Festival resolveFestivalById(Long festivalId){
        return festivalRepo.findById(festivalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found: id="+festivalId));
    }

    // ========= PRIMARY ENDPOINT =========
    // POST /api/performances/festival/{festivalId}
    @PostMapping("/festival/{festivalId}")
    public ResponseEntity<?> createForFestival(@PathVariable Long festivalId,
                                               @RequestBody Map<String,Object> body,
                                               Authentication authentication){
        // 1) Validate basic fields
        String name  = str(body.get("name"));
        String genre = str(body.get("genre"));
        if (isBlank(name))  return bad("Field 'name' is required");
        if (isBlank(genre)) return bad("Field 'genre' is required");

        Duration dur = parseDuration(body);
        if (dur == null || dur.isZero() || dur.isNegative()){
            return bad("Provide 'durationMinutes' (>0) or 'duration' in seconds (>0)");
        }

        // 2) Resolve festival, main artist
        Festival festival = resolveFestivalById(festivalId);
        User mainArtist   = resolveMainArtist(body, authentication);

        // 3) Conflict check (name per festival)
        try{
            if (performanceRepo.existsByNameIgnoreCaseAndFestivalId(name.trim(), festivalId)){
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error","Duplicate performance name for this festival","name",name));
            }
        }catch (NoSuchMethodError | RuntimeException ignore){ /* unique constraint will protect anyway */ }

        // 4) Build + save
        try{
            Performance p = new Performance();
            p.setName(name.trim());
            p.setGenre(genre.trim());
            p.setDescription(Optional.ofNullable(str(body.get("description"))).map(String::trim).filter(s->!s.isEmpty()).orElse(null));
            p.setDuration(dur);
            p.setFestival(festival);
            p.setMainArtist(mainArtist);

            // status (optional)
            p.setStatus(parseStatus(body.get("status")));
            p.setCreatedAt(LocalDateTime.now());

            Performance saved = performanceRepo.save(p);
            return ResponseEntity.created(URI.create("/api/performances/"+saved.getId())).body(saved);

        }catch (DataIntegrityViolationException dive){
            return ResponseEntity.badRequest().body(Map.of(
                    "error","Constraint violation",
                    "details", mostSpecific(dive)
            ));
        }catch (Exception ex){
            return ResponseEntity.internalServerError().body(Map.of(
                    "error","Failed to create performance",
                    "details", ex.getMessage()
            ));
        }
    }

    // ========= FALLBACK ENDPOINT =========
    // POST /api/performances  (με { festivalId } στο σώμα)
    @PostMapping
    public ResponseEntity<?> createGeneric(@RequestBody Map<String,Object> body, Authentication authentication){
        Long festivalId = toLong(body.get("festivalId"));
        if (festivalId == null) return bad("Field 'festivalId' is required");
        return createForFestival(festivalId, body, authentication);
    }
// GET /api/performances  ?status=PENDING|SUBMITTED|APPROVED|ALL  &q=term  &page=0&size=20

@GetMapping("/{status}")
public ResponseEntity<Page<Performance>> listByPath(
        @PathVariable String status,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
) {
    // ίδιο implementation με το list(...)
    if ("ALL".equalsIgnoreCase(status)) {
        Page<Performance> page = (q != null && !q.isBlank())
                ? performanceRepo.searchByStatuses(Arrays.asList(PerformanceStatus.values()), q, pageable)
                : performanceRepo.findAll(pageable);
        return ResponseEntity.ok(page);
    }
    try {
        PerformanceStatus st = PerformanceStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        Page<Performance> page = (q != null && !q.isBlank())
                ? performanceRepo.searchByStatus(st, q, pageable)
                : performanceRepo.findByStatusIn(List.of(st), pageable);
        return ResponseEntity.ok(page);
    } catch (IllegalArgumentException ex) {
        Page<Performance> page = (q != null && !q.isBlank())
                ? performanceRepo.searchByStatuses(Arrays.asList(PerformanceStatus.values()), q, pageable)
                : performanceRepo.findAll(pageable);
        return ResponseEntity.ok(page);
    }
}

@PostMapping(
  value = "/{performanceId}/review",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<?> review(
        @PathVariable Long performanceId,
        @RequestBody Map<String,Object> body,
        Authentication authentication
){
    if (authentication == null || authentication.getName() == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","No auth"));
    }

    var reviewer = userRepo.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    Integer score = null;
    try {
        Object s = body.get("score");
        if (s instanceof Number n) score = n.intValue();
        else if (s != null) score = Integer.parseInt(String.valueOf(s));
    } catch(Exception ignore){}

    String comments = Optional.ofNullable(body.get("comments")).map(String::valueOf).orElse(null);

    if (score == null || score < 1 || score > 10) {
        return ResponseEntity.badRequest().body(Map.of("error","Score must be between 1 and 10"));
    }

    var perf = performanceService.reviewPerformance(performanceId, reviewer, score, comments);

    // Μικρή, καθαρή απόκριση
    return ResponseEntity.ok(Map.of(
            "ok", true,
            "performanceId", perf.getId(),
            "review", Map.of("score", score, "comments", comments, "reviewer", reviewer.getUsername())
    ));
}
@GetMapping
public ResponseEntity<?> list(
        @RequestParam(required = false, defaultValue = "ALL") String status,
        @RequestParam(required = false) String q,
        Pageable pageable
){
    if ("ALL".equalsIgnoreCase(status)) {
        Page<Performance> page = (q != null && !q.isBlank())
                ? performanceRepo.searchByStatuses(Arrays.asList(PerformanceStatus.values()), q, pageable)
                : performanceRepo.findAll(pageable);
        return ResponseEntity.ok(page);
    }
    try {
        PerformanceStatus st = PerformanceStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        Page<Performance> page = (q != null && !q.isBlank())
                ? performanceRepo.searchByStatus(st, q, pageable)
                : performanceRepo.findByStatusIn(List.of(st), pageable);
        return ResponseEntity.ok(page);
    } catch (IllegalArgumentException ex) {
        Page<Performance> page = (q != null && !q.isBlank())
                ? performanceRepo.searchByStatuses(Arrays.asList(PerformanceStatus.values()), q, pageable)
                : performanceRepo.findAll(pageable);
        return ResponseEntity.ok(page);
    }
}


    // ======= Παράδειγμα existing read endpoint =======
    @GetMapping("/approved")
    public List<Performance> getApproved() {
        return performanceService.getApprovedPerformances();
    }
}
