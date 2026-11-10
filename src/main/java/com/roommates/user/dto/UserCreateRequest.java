package com.roommates.user.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

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

        @NotEmpty
        Set<String> roles,

        Boolean enabled
) {
}
