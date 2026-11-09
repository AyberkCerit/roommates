package com.roommates.user.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.roommates.user.Role;
import com.roommates.user.User;

/**
 * KULLANICI CEVABI: istemciye dönen bilgi.
 *
 * Örnek JSON:
 *   {
 *     "id": 5,
 *     "username": "ayse",
 *     "email": "ayse@roommates.local",
 *     "displayName": "Ayşe",
 *     "enabled": true,
 *     "createdAt": "2026-11-09T18:50:24Z",
 *     "roles": ["ROLE_VIEWER"]
 *   }
 *
 * PAROLA ALANI YOK: şifreli hâli bile dışarı gönderilmez.
 *
 * Neden User entity'sini doğrudan döndürmüyoruz?
 *   Entity veritabanı tablosunun birebir kopyası; içinde parola var.
 *   DTO (bu sınıf) ile dışarıya sadece göstermek istediğimizi veriyoruz.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        boolean enabled,
        Instant createdAt,
        List<String> roles
) {

    /** User entity'sini UserResponse'a çevirir. */
    public static UserResponse from(User kullanici) {

        // Rol nesnelerinden sadece adlarını al ve alfabetik sırala.
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
