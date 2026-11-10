package com.roommates.room.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.roommates.room.RoomType;
import com.roommates.room.RoomVisibility;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull RoomType type,
        @NotNull RoomVisibility visibility,
        @NotNull @Min(2) @Max(50) Integer maxMembers,
        @Future Instant closesAt
) {
}
