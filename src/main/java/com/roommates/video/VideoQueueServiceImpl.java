package com.roommates.video;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roommates.common.error.InvalidRequestException;
import com.roommates.common.error.ResourceNotFoundException;
import com.roommates.room.Room;
import com.roommates.room.RoomRepository;
import com.roommates.room.RoomType;
import com.roommates.user.User;
import com.roommates.user.UserRepository;
import com.roommates.video.dto.VideoCreateRequest;
import com.roommates.video.dto.VideoPatchRequest;
import com.roommates.video.dto.VideoResponse;
import com.roommates.video.dto.VideoUpdateRequest;

@Service
public class VideoQueueServiceImpl implements VideoQueueService {
    private final VideoQueueItemRepository videoDeposu;
    private final RoomRepository odaDeposu;
    private final UserRepository kullaniciDeposu;
    private final Clock saat;

    public VideoQueueServiceImpl(VideoQueueItemRepository videoDeposu,
                                 RoomRepository odaDeposu,
                                 UserRepository kullaniciDeposu,
                                 Clock saat) {
        this.videoDeposu = videoDeposu;
        this.odaDeposu = odaDeposu;
        this.kullaniciDeposu = kullaniciDeposu;
        this.saat = saat;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoResponse> findAll(Long roomId) {
        odayiBul(roomId);

        List<VideoResponse> cevaplar = new ArrayList<>();
        for (VideoQueueItem video : videoDeposu.findByRoomId(roomId)) {
            cevaplar.add(VideoResponse.from(video));
        }
        return cevaplar;
    }

    @Override
    @Transactional(readOnly = true)
    public VideoResponse findById(Long roomId, Long videoId) {
        odayiBul(roomId);
        return VideoResponse.from(videoyuBul(roomId, videoId));
    }

    @Override
    @Transactional
    public VideoResponse create(Long roomId, VideoCreateRequest istek, String username) {
        Room oda = odayiBul(roomId);
        if (oda.getType() != RoomType.WATCH) {
            throw new InvalidRequestException("Video yalnız izleme odasına eklenebilir");
        }

        Optional<User> bulunan = kullaniciDeposu.findByUsername(username);
        if (bulunan.isEmpty()) {
            throw new InvalidRequestException("Giriş yapan kullanıcı bulunamadı");
        }
        User ekleyen = bulunan.get();

        int videoSayisi = videoDeposu.countByRoomId(roomId);
        int sira = videoSayisi + 1;
        if (istek.position() != null) {
            sira = istek.position();
        }
        if (sira < 1 || sira > videoSayisi + 1) {
            throw new InvalidRequestException("Geçersiz sıra: 1 ile " + (videoSayisi + 1) + " arasında olmalı");
        }

        videoDeposu.shiftPositions(roomId, sira, videoSayisi, 1);

        VideoQueueItem yeniVideo = new VideoQueueItem(istek.youtubeVideoId(), istek.title(), sira,
                oda, ekleyen, Instant.now(saat));
        videoDeposu.save(yeniVideo);

        return VideoResponse.from(yeniVideo);
    }

    @Override
    @Transactional
    public VideoResponse update(Long roomId, Long videoId, VideoUpdateRequest istek) {
        odayiBul(roomId);
        VideoQueueItem video = videoyuBul(roomId, videoId);

        siraDegistir(video, istek.position());
        video.setYoutubeVideoId(istek.youtubeVideoId());
        video.setTitle(istek.title());

        VideoQueueItem guncel = videoDeposu.update(video);
        return VideoResponse.from(guncel);
    }

    @Override
    @Transactional
    public VideoResponse patch(Long roomId, Long videoId, VideoPatchRequest istek) {
        odayiBul(roomId);
        VideoQueueItem video = videoyuBul(roomId, videoId);

        if (istek.id() != null) {
            throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
        }

        if (istek.position() != null) {
            siraDegistir(video, istek.position());
        }
        if (istek.youtubeVideoId() != null) {
            video.setYoutubeVideoId(istek.youtubeVideoId());
        }
        if (istek.title() != null) {
            video.setTitle(istek.title());
        }

        VideoQueueItem guncel = videoDeposu.update(video);
        return VideoResponse.from(guncel);
    }

    @Override
    @Transactional
    public void delete(Long roomId, Long videoId) {
        odayiBul(roomId);
        VideoQueueItem video = videoyuBul(roomId, videoId);

        int eskiSira = video.getPosition();
        int videoSayisi = videoDeposu.countByRoomId(roomId);

        videoDeposu.delete(video);
        videoDeposu.shiftPositions(roomId, eskiSira + 1, videoSayisi, -1);
    }

    private void siraDegistir(VideoQueueItem video, int yeniSira) {
        Long roomId = video.getRoom().getId();
        int eskiSira = video.getPosition();
        int videoSayisi = videoDeposu.countByRoomId(roomId);

        if (yeniSira < 1 || yeniSira > videoSayisi) {
            throw new InvalidRequestException("Geçersiz sıra: 1 ile " + videoSayisi + " arasında olmalı");
        }

        if (yeniSira < eskiSira) {
            videoDeposu.shiftPositions(roomId, yeniSira, eskiSira - 1, 1);
        }
        if (yeniSira > eskiSira) {
            videoDeposu.shiftPositions(roomId, eskiSira + 1, yeniSira, -1);
        }
        video.setPosition(yeniSira);
    }

    private Room odayiBul(Long roomId) {
        Optional<Room> bulunan = odaDeposu.findById(roomId);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Oda", roomId);
        }
        return bulunan.get();
    }

    private VideoQueueItem videoyuBul(Long roomId, Long videoId) {
        Optional<VideoQueueItem> bulunan = videoDeposu.findById(videoId);
        if (bulunan.isEmpty() || !bulunan.get().getRoom().getId().equals(roomId)) {
            throw new ResourceNotFoundException("Video", videoId);
        }
        return bulunan.get();
    }
}
