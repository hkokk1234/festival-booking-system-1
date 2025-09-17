package com.example.festival_management.service;

import com.example.festival_management.entity.Festival;
import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.FestivalState;
import com.example.festival_management.repository.FestivalRepository; // <-- για το nested Option

import java.util.List;
import java.util.Optional;

public interface FestivalService {
    Festival createFestival(Festival festival, User creator);
    Festival updateFestival(Long id, Festival festival, User user);
    void deleteFestival(Long id, User user);
    Optional<Festival> getFestivalById(Long id);
    List<Festival> searchFestivals(String name, String description, String venue, String dates, User user);
    void changeFestivalState(Long id, FestivalState newState, User user);
    void addOrganizers(Long id, List<User> users, User user);
    void addStaff(Long id, List<User> users, User user);

    /** ΝΕΟ: επιστρέφει (id, name) για το combobox */
     List<Festival> findAll();
    List<FestivalRepository.Option> options();
}
