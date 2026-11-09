package com.roommates.user.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * YENİ KULLANICI EKLEME İSTEĞİ (POST /api/users)
 *
 * Örnek JSON:
 *   {
 *     "username": "ayse",
 *     "password": "Parola.123",
 *     "email": "ayse@roommates.local",
 *     "displayName": "Ayşe",
 *     "roles": ["ROLE_VIEWER"],
 *     "enabled": true
 *   }
 *
 * "id" alanı YOK: numarayı veritabanı verir. İstemci id gönderse bile
 * @JsonIgnoreProperties(ignoreUnknown = true) sayesinde yok sayılır (M23).
 *
 * Alanların üstündeki kurallar (@NotBlank, @Size...) uyulmazsa 400 döner.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCreateRequest(

        @NotBlank @Size(max = 50)
        String username,

        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotBlank @Email @Size(max = 100)
        String email,

        @NotBlank @Size(max = 100)
        String displayName,

        @NotEmpty                 // en az bir rol olmalı
        Set<String> roles,

        Boolean enabled           // gönderilmezse (null) aktif kabul edilir
) {
}
