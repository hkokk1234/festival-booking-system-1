package com.example.festival_management.dto;

import com.example.festival_management.entity.User;
import com.example.festival_management.entity.enums.RoleType;

public record UserDto(
        Long id,
        String username,
        String email,
        java.util.List<String> roles
) {
    public static UserDto from(User u){
        var roleNames =
            (u.getRoleAssignments() == null)
            ? java.util.List.<String>of()
            : u.getRoleAssignments().stream()
                .map(ra -> ra.getRole())             // RoleType (enum) ή nullable
                .filter(java.util.Objects::nonNull)
                .map(RoleType::name)                  // "ADMIN", "ARTIST", ...
                .distinct()
                .toList();

        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), roleNames);
    }
}
