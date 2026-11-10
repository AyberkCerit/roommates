package com.roommates.room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    Room save(Room room);

    Optional<Room> findById(Long id);

    List<Room> findAll();

    Room update(Room room);

    void delete(Room room);

    List<Room> findByFilter(RoomType type, RoomVisibility visibility);
}
