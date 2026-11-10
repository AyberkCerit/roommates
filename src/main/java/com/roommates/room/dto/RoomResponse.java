package com.roommates.room.dto;

import java.time.Instant;

import com.roommates.room.Room;
import com.roommates.room.RoomType;
import com.roommates.room.RoomVisibility;

public record RoomResponse(
        Long id,
        String name,
        String description,
        RoomType type,
        RoomVisibility visibility,
        Integer maxMembers,
        Instant createdAt,
        Instant closesAt,
        Long ownerId,
        String ownerUsername
) {
    public static RoomResponse from(Room oda) {
        return new RoomResponse(
                oda.getId(),
                oda.getName(),
                oda.getDescription(),
                oda.getType(),
                oda.getVisibility(),
                oda.getMaxMembers(),
                oda.getCreatedAt(),
                oda.getClosesAt(),
                oda.getOwner().getId(),
                oda.getOwner().getUsername());
    }
}
