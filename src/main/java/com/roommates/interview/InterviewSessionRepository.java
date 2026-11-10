package com.roommates.interview;

import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository {
    InterviewSession save(InterviewSession session);

    Optional<InterviewSession> findById(Long id);

    List<InterviewSession> findAll();

    InterviewSession update(InterviewSession session);

    void delete(InterviewSession session);

    List<InterviewSession> findByStatus(InterviewStatus status);
}
