package com.example.festival_management.service.impl;

import com.example.festival_management.dto.UserDto;
import com.example.festival_management.entity.User;
import com.example.festival_management.repository.UserRepository;
import com.example.festival_management.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.festival_management.dto.UserDto;

import java.util.List;
import java.util.Optional;
 // Ylopoiisi userservices me kanones asfaleias/epixirisiakis logikis

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Χειροκίνητος constructor injection
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
  // repositories pou xreiazontai
//methodoi kai energeies pou aforoun xrhstes
    @Override
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
   


     @Override
    public List<User> getAllUsers() {
        return userRepository.findAllWithRolesAndAssignments(); // <-- ΧΡΗΣΗ fetch join
    }
}
