package com.roommates.video;

import java.util.List;

import com.roommates.video.dto.VideoCreateRequest;
import com.roommates.video.dto.VideoPatchRequest;
import com.roommates.video.dto.VideoResponse;
import com.roommates.video.dto.VideoUpdateRequest;

public interface VideoQueueService {
    List<VideoResponse> findAll(Long roomId);

    VideoResponse findById(Long roomId, Long videoId);

    VideoResponse create(Long roomId, VideoCreateRequest request, String username);

    VideoResponse update(Long roomId, Long videoId, VideoUpdateRequest request);

    VideoResponse patch(Long roomId, Long videoId, VideoPatchRequest request);

    void delete(Long roomId, Long videoId);
}
