package com.roommates.room;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

@Repository
public class RoomRepositoryImpl implements RoomRepository {
    private final EntityManager entityManager;

    public RoomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Room save(Room room) {
        entityManager.persist(room);
        return room;
    }

    @Override
    public Optional<Room> findById(Long id) {
        Room oda = entityManager.find(Room.class, id);
        return Optional.ofNullable(oda);
    }

    @Override
    public List<Room> findAll() {
        return entityManager
                .createQuery("SELECT r FROM Room r ORDER BY r.id", Room.class)
                .getResultList();
    }

    @Override
    public Room update(Room room) {
        return entityManager.merge(room);
    }

    @Override
    public void delete(Room room) {
        entityManager.remove(room);
    }

    @Override
    public List<Room> findByFilter(RoomType type, RoomVisibility visibility) {
        return entityManager
                .createQuery("SELECT r FROM Room r "
                        + "WHERE (:type IS NULL OR r.type = :type) "
                        + "AND (:visibility IS NULL OR r.visibility = :visibility) "
                        + "ORDER BY r.id", Room.class)
                .setParameter("type", type)
                .setParameter("visibility", visibility)
                .getResultList();
    }
}
