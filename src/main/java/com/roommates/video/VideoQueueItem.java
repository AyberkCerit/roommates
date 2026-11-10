package com.roommates.video;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.roommates.room.Room;
import com.roommates.user.User;

@Entity
@Table(name = "video_queue_items")
public class VideoQueueItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "youtube_video_id", nullable = false, length = 20)
    private String youtubeVideoId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by_id", nullable = false)
    private User addedBy;

    protected VideoQueueItem() {
    }

    public VideoQueueItem(String youtubeVideoId, String title, Integer position,
                          Room room, User addedBy, Instant addedAt) {
        this.youtubeVideoId = youtubeVideoId;
        this.title = title;
        this.position = position;
        this.room = room;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public Room getRoom() {
        return room;
    }

    public User getAddedBy() {
        return addedBy;
    }
}
