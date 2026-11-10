package com.roommates.video.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoCreateRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{11}", message = "11 karakterlik bir YouTube video kimliği olmalı")
        String youtubeVideoId,
        @NotBlank @Size(max = 200) String title,
        @Min(1) Integer position
) {
}
