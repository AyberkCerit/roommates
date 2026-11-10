package com.roommates.study.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.roommates.study.StudySessionStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StudySessionUpdateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull Long roomId,
        @NotNull @Min(1) @Max(180) Integer focusMinutes,
        @NotNull @Min(1) @Max(60) Integer breakMinutes,
        @NotNull @Min(1) @Max(12) Integer rounds,
        @NotNull StudySessionStatus status
) {
}
