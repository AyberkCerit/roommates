package com.roommates.user.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * KULLANICIYI TAMAMEN GÜNCELLEME İSTEĞİ (PUT /api/users/{id})  (M24)
 *
 * PUT = tam güncelleme: BÜTÜN alanlar gönderilmek zorunda, parola dahil.
 * Eksik alan varsa 400 döner.
 * Hangi kullanıcının güncelleneceğini sadece adresteki {id} belirler.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserUpdateRequest(

        @NotBlank @Size(max = 50)
        String username,

        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotBlank @Email @Size(max = 100)
        String email,

        @NotBlank @Size(max = 100)
        String displayName,

        @NotEmpty
        Set<String> roles,

        @NotNull                  // PUT'ta aktiflik bilgisi de zorunlu
        Boolean enabled
) {
}
