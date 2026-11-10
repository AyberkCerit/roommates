package com.roommates.room;

import java.util.List;

import com.roommates.room.dto.RoomCreateRequest;
import com.roommates.room.dto.RoomPatchRequest;
import com.roommates.room.dto.RoomResponse;
import com.roommates.room.dto.RoomUpdateRequest;

public interface RoomService {
    List<RoomResponse> findAll(RoomType type, RoomVisibility visibility);

    RoomResponse findById(Long id);

    RoomResponse create(RoomCreateRequest request, String username);

    RoomResponse update(Long id, RoomUpdateRequest request);

    RoomResponse patch(Long id, RoomPatchRequest request);

    void delete(Long id);
}
