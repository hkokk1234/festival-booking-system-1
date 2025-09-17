package com.example.festival_management.service;

import com.example.festival_management.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findByUsername(String username);
    List<User> getAllUsers();
    boolean usernameExists(String username);
    boolean emailExists(String email);
    
}
