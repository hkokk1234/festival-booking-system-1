package com.example.festival_management;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.FestivalState;
import com.example.festival_management.repository.FestivalRepository;
import com.example.festival_management.repository.RoleAssignmentRepository;
import com.example.festival_management.service.impl.FestivalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FestivalServiceTest {

    private FestivalRepository festivalRepository;
    private RoleAssignmentRepository roleAssignmentRepository;

    private FestivalServiceImpl festivalService;

    private User organizer;

    @BeforeEach
    void setUp() {
        festivalRepository = mock(FestivalRepository.class);
        roleAssignmentRepository = mock(RoleAssignmentRepository.class);
        festivalService = new FestivalServiceImpl(festivalRepository, roleAssignmentRepository);

        organizer = new User();
        // Αν χρειαστείς id/username, μπορείς να τα ορίσεις εδώ αν υπάρχουν setters στο entity σου
        // organizer.setId(1L);
        // organizer.setUsername("organizer1");
    }

    @Test
    void testCreateFestival() {
        Festival festival = new Festival();
        festival.setName("Athens Fest");
        festival.setDescription("Annual music event");

        when(festivalRepository.existsByName("Athens Fest")).thenReturn(false);
        when(festivalRepository.save(any(Festival.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Festival result = festivalService.createFestival(festival, organizer);

        ArgumentCaptor<Festival> captor = ArgumentCaptor.forClass(Festival.class);
        verify(festivalRepository).save(captor.capture());
        Festival saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Athens Fest");
        assertThat(saved.getState()).isEqualTo(FestivalState.CREATED);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(result).isNotNull();

        // Ελέγχει ότι δημιουργήθηκε και role assignment (ο οργανωτής)
        verify(roleAssignmentRepository, times(1)).save(any());
    }

    @Test
    void testUpdateFestival() {
        // existing festival
        Festival existing = new Festival();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setDescription("Old Desc");
        existing.setState(FestivalState.CREATED);

        // updated payload
        Festival updated = new Festival();
        updated.setName("New Name");
        updated.setDescription("New Desc");
        updated.setVenue("New Venue");

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(existing));
        // Ο χρήστης θεωρείται organizer
        when(roleAssignmentRepository.existsByUserAndFestivalAndRole(any(), eq(existing), any()))
                .thenReturn(true);
        when(festivalRepository.save(any(Festival.class))).thenAnswer(i -> i.getArgument(0));

        Festival result = festivalService.updateFestival(1L, updated, organizer);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Desc");
        assertThat(result.getVenue()).isEqualTo("New Venue");
    }
}
