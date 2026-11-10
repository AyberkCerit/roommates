package com.roommates.video.dto;

import java.time.Instant;

import com.roommates.video.VideoQueueItem;

public record VideoResponse(
        Long id,
        Long roomId,
        String youtubeVideoId,
        String title,
        Integer position,
        Instant addedAt,
        String addedByUsername
) {
    public static VideoResponse from(VideoQueueItem video) {
        return new VideoResponse(
                video.getId(),
                video.getRoom().getId(),
                video.getYoutubeVideoId(),
                video.getTitle(),
                video.getPosition(),
                video.getAddedAt(),
                video.getAddedBy().getUsername());
    }
}
