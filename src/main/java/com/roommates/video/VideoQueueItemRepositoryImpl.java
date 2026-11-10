package com.roommates.video;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

@Repository
public class VideoQueueItemRepositoryImpl implements VideoQueueItemRepository {
    private final EntityManager entityManager;

    public VideoQueueItemRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public VideoQueueItem save(VideoQueueItem item) {
        entityManager.persist(item);
        return item;
    }

    @Override
    public Optional<VideoQueueItem> findById(Long id) {
        VideoQueueItem video = entityManager.find(VideoQueueItem.class, id);
        return Optional.ofNullable(video);
    }

    @Override
    public VideoQueueItem update(VideoQueueItem item) {
        return entityManager.merge(item);
    }

    @Override
    public void delete(VideoQueueItem item) {
        entityManager.remove(item);
        entityManager.flush();
    }

    @Override
    public List<VideoQueueItem> findByRoomId(Long roomId) {
        return entityManager
                .createQuery("SELECT v FROM VideoQueueItem v "
                        + "WHERE v.room.id = :roomId ORDER BY v.position", VideoQueueItem.class)
                .setParameter("roomId", roomId)
                .getResultList();
    }

    @Override
    public int countByRoomId(Long roomId) {
        Long sayi = entityManager
                .createQuery("SELECT COUNT(v) FROM VideoQueueItem v WHERE v.room.id = :roomId", Long.class)
                .setParameter("roomId", roomId)
                .getSingleResult();
        return sayi.intValue();
    }

    @Override
    public int deleteByRoomId(Long roomId) {
        return entityManager
                .createQuery("DELETE FROM VideoQueueItem v WHERE v.room.id = :roomId")
                .setParameter("roomId", roomId)
                .executeUpdate();
    }

    @Override
    public int shiftPositions(Long roomId, int fromInclusive, int toInclusive, int delta) {
        if (fromInclusive > toInclusive) {
            return 0;
        }
        entityManager.flush();
        return entityManager
                .createQuery("UPDATE VideoQueueItem v SET v.position = v.position + :delta "
                        + "WHERE v.room.id = :roomId "
                        + "AND v.position BETWEEN :fromInclusive AND :toInclusive")
                .setParameter("delta", delta)
                .setParameter("roomId", roomId)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toInclusive", toInclusive)
                .executeUpdate();
    }
}
