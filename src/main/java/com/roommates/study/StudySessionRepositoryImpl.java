package com.roommates.study;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

@Repository
public class StudySessionRepositoryImpl implements StudySessionRepository {
    private final EntityManager entityManager;

    public StudySessionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public StudySession save(StudySession session) {
        entityManager.persist(session);
        return session;
    }

    @Override
    public Optional<StudySession> findById(Long id) {
        StudySession oturum = entityManager.find(StudySession.class, id);
        return Optional.ofNullable(oturum);
    }

    @Override
    public List<StudySession> findAll() {
        return entityManager
                .createQuery("SELECT s FROM StudySession s ORDER BY s.id", StudySession.class)
                .getResultList();
    }

    @Override
    public StudySession update(StudySession session) {
        return entityManager.merge(session);
    }

    @Override
    public void delete(StudySession session) {
        entityManager.remove(session);
    }

    @Override
    public List<StudySession> findByFilter(Long roomId, StudySessionStatus status) {
        return entityManager
                .createQuery("SELECT s FROM StudySession s "
                        + "WHERE (:roomId IS NULL OR s.room.id = :roomId) "
                        + "AND (:status IS NULL OR s.status = :status) "
                        + "ORDER BY s.id", StudySession.class)
                .setParameter("roomId", roomId)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<StudySession> findByStatus(StudySessionStatus status) {
        return entityManager
                .createQuery("SELECT s FROM StudySession s WHERE s.status = :status ORDER BY s.id",
                        StudySession.class)
                .setParameter("status", status)
                .getResultList();
    }
}
