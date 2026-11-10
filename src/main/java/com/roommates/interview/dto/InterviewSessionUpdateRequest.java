package com.roommates.interview.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.roommates.interview.InterviewCategory;
import com.roommates.interview.InterviewStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterviewSessionUpdateRequest(
        @NotNull Long roomId,
        @NotNull Long interviewerId,
        @NotNull Long candidateId,
        @NotBlank @Size(max = 100) String topic,
        @NotNull InterviewCategory category,
        @NotBlank @Size(max = 500) String question,
        @NotNull InterviewStatus status,
        @NotNull Instant scheduledAt
) {
}
