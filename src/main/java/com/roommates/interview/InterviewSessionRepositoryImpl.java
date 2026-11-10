package com.roommates.interview;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

@Repository
public class InterviewSessionRepositoryImpl implements InterviewSessionRepository {
    private final EntityManager entityManager;

    public InterviewSessionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public InterviewSession save(InterviewSession session) {
        entityManager.persist(session);
        return session;
    }

    @Override
    public Optional<InterviewSession> findById(Long id) {
        InterviewSession mulakat = entityManager.find(InterviewSession.class, id);
        return Optional.ofNullable(mulakat);
    }

    @Override
    public List<InterviewSession> findAll() {
        return entityManager
                .createQuery("SELECT i FROM InterviewSession i ORDER BY i.id", InterviewSession.class)
                .getResultList();
    }

    @Override
    public InterviewSession update(InterviewSession session) {
        return entityManager.merge(session);
    }

    @Override
    public void delete(InterviewSession session) {
        entityManager.remove(session);
    }

    @Override
    public List<InterviewSession> findByStatus(InterviewStatus status) {
        return entityManager
                .createQuery("SELECT i FROM InterviewSession i "
                        + "WHERE (:status IS NULL OR i.status = :status) "
                        + "ORDER BY i.id", InterviewSession.class)
                .setParameter("status", status)
                .getResultList();
    }
}
