package com.roommates.user.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * KULLANICIYI KISMEN GÜNCELLEME İSTEĞİ (PATCH /api/users/{id})  (M25)
 *
 * Bütün alanlar isteğe bağlı. Sadece GÖNDERİLEN alanlar değişir,
 * gönderilmeyenler (null olanlar) olduğu gibi kalır.
 *
 * Örnek: {"password": "YeniParola.1"} → sadece parola değişir.
 *
 * "id" alanı neden var?
 *   Sadece REDDETMEK için. Biri PATCH ile id'yi değiştirmeye çalışırsa
 *   (id null değilse) 400 döneriz: "Birincil anahtar (id) PATCH ile değiştirilemez".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserPatchRequest(

        Long id,                      // gelirse reddedilir

        @Size(min = 1, max = 50)
        String username,

        @Size(min = 8, max = 72)
        String password,

        @Email @Size(max = 100)
        String email,

        @Size(min = 1, max = 100)
        String displayName,

        @Size(min = 1)
        Set<String> roles,

        Boolean enabled
) {
}
