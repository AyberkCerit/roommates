package com.roommates.interview.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.roommates.interview.InterviewCategory;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterviewSessionCreateRequest(
        @NotNull Long roomId,
        @NotNull Long interviewerId,
        @NotNull Long candidateId,
        @NotBlank @Size(max = 100) String topic,
        @NotNull InterviewCategory category,
        @NotNull Instant scheduledAt
) {
}
