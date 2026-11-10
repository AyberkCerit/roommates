package com.roommates.study;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository {
    StudySession save(StudySession session);

    Optional<StudySession> findById(Long id);

    List<StudySession> findAll();

    StudySession update(StudySession session);

    void delete(StudySession session);

    List<StudySession> findByFilter(Long roomId, StudySessionStatus status);

    List<StudySession> findByStatus(StudySessionStatus status);
}
