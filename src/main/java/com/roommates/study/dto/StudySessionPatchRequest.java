package com.roommates.study.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.roommates.study.StudySessionStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StudySessionPatchRequest(
        Long id,
        @Size(min = 1, max = 100) String title,
        Long roomId,
        @Min(1) @Max(180) Integer focusMinutes,
        @Min(1) @Max(60) Integer breakMinutes,
        @Min(1) @Max(12) Integer rounds,
        StudySessionStatus status
) {
}
