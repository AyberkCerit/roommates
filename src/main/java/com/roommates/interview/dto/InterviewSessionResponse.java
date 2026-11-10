package com.roommates.interview.dto;

import java.time.Instant;

import com.roommates.interview.InterviewCategory;
import com.roommates.interview.InterviewSession;
import com.roommates.interview.InterviewStatus;

public record InterviewSessionResponse(
        Long id,
        Long roomId,
        Long interviewerId,
        String interviewerUsername,
        Long candidateId,
        String candidateUsername,
        String topic,
        InterviewCategory category,
        String question,
        InterviewStatus status,
        Instant scheduledAt
) {
    public static InterviewSessionResponse from(InterviewSession mulakat) {
        return new InterviewSessionResponse(
                mulakat.getId(),
                mulakat.getRoom().getId(),
                mulakat.getInterviewer().getId(),
                mulakat.getInterviewer().getUsername(),
                mulakat.getCandidate().getId(),
                mulakat.getCandidate().getUsername(),
                mulakat.getTopic(),
                mulakat.getCategory(),
                mulakat.getQuestion(),
                mulakat.getStatus(),
                mulakat.getScheduledAt());
    }
}
