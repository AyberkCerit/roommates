package com.roommates.user.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.roommates.user.Role;
import com.roommates.user.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        boolean enabled,
        Instant createdAt,
        List<String> roles
) {
    public static UserResponse from(User kullanici) {
        List<String> rolAdlari = new ArrayList<>();
        for (Role rol : kullanici.getRoles()) {
            rolAdlari.add(rol.getName());
        }
        Collections.sort(rolAdlari);

        return new UserResponse(
                kullanici.getId(),
                kullanici.getUsername(),
                kullanici.getEmail(),
                kullanici.getDisplayName(),
                kullanici.isEnabled(),
                kullanici.getCreatedAt(),
                rolAdlari);
    }
}
