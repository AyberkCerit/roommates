package com.roommates.user.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserPatchRequest(

        Long id,

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
