package com.roommates.interview.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Size;

import com.roommates.interview.InterviewCategory;
import com.roommates.interview.InterviewStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterviewSessionPatchRequest(
        Long id,
        Long roomId,
        Long interviewerId,
        Long candidateId,
        @Size(min = 1, max = 100) String topic,
        InterviewCategory category,
        @Size(min = 1, max = 500) String question,
        InterviewStatus status,
        Instant scheduledAt
) {
}
