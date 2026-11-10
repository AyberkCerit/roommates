package com.roommates.video;

import java.util.List;
import java.util.Optional;

public interface VideoQueueItemRepository {
    VideoQueueItem save(VideoQueueItem item);

    Optional<VideoQueueItem> findById(Long id);

    VideoQueueItem update(VideoQueueItem item);

    void delete(VideoQueueItem item);

    List<VideoQueueItem> findByRoomId(Long roomId);

    int countByRoomId(Long roomId);

    int deleteByRoomId(Long roomId);

    int shiftPositions(Long roomId, int fromInclusive, int toInclusive, int delta);
}
