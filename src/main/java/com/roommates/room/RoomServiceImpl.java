package com.roommates.room;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roommates.common.error.InvalidRequestException;
import com.roommates.common.error.ResourceNotFoundException;
import com.roommates.room.dto.RoomCreateRequest;
import com.roommates.room.dto.RoomPatchRequest;
import com.roommates.room.dto.RoomResponse;
import com.roommates.room.dto.RoomUpdateRequest;
import com.roommates.user.User;
import com.roommates.user.UserRepository;
import com.roommates.video.VideoQueueItemRepository;

@Service
public class RoomServiceImpl implements RoomService {
    private final RoomRepository odaDeposu;
    private final UserRepository kullaniciDeposu;
    private final VideoQueueItemRepository videoDeposu;
    private final Clock saat;

    public RoomServiceImpl(RoomRepository odaDeposu,
                           UserRepository kullaniciDeposu,
                           VideoQueueItemRepository videoDeposu,
                           Clock saat) {
        this.odaDeposu = odaDeposu;
        this.kullaniciDeposu = kullaniciDeposu;
        this.videoDeposu = videoDeposu;
        this.saat = saat;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAll(RoomType type, RoomVisibility visibility) {
        List<Room> odalar = odaDeposu.findByFilter(type, visibility);

        List<RoomResponse> cevaplar = new ArrayList<>();
        for (Room oda : odalar) {
            cevaplar.add(RoomResponse.from(oda));
        }
        return cevaplar;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        return RoomResponse.from(odayiBul(id));
    }

    @Override
    @Transactional
    public RoomResponse create(RoomCreateRequest istek, String username) {
        Optional<User> bulunan = kullaniciDeposu.findByUsername(username);
        if (bulunan.isEmpty()) {
            throw new InvalidRequestException("Giriş yapan kullanıcı bulunamadı");
        }
        User sahip = bulunan.get();

        Room yeniOda = new Room(istek.name(), istek.description(), istek.type(), istek.visibility(),
                istek.maxMembers(), istek.closesAt(), sahip, Instant.now(saat));
        odaDeposu.save(yeniOda);

        return RoomResponse.from(yeniOda);
    }

    @Override
    @Transactional
    public RoomResponse update(Long id, RoomUpdateRequest istek) {
        Room oda = odayiBul(id);

        oda.setName(istek.name());
        oda.setDescription(istek.description());
        oda.setType(istek.type());
        oda.setVisibility(istek.visibility());
        oda.setMaxMembers(istek.maxMembers());
        oda.setClosesAt(istek.closesAt());

        Room guncel = odaDeposu.update(oda);
        return RoomResponse.from(guncel);
    }

    @Override
    @Transactional
    public RoomResponse patch(Long id, RoomPatchRequest istek) {
        Room oda = odayiBul(id);

        if (istek.id() != null) {
            throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
        }

        if (istek.name() != null) {
            oda.setName(istek.name());
        }
        if (istek.description() != null) {
            oda.setDescription(istek.description());
        }
        if (istek.type() != null) {
            oda.setType(istek.type());
        }
        if (istek.visibility() != null) {
            oda.setVisibility(istek.visibility());
        }
        if (istek.maxMembers() != null) {
            oda.setMaxMembers(istek.maxMembers());
        }
        if (istek.closesAt() != null) {
            oda.setClosesAt(istek.closesAt());
        }

        Room guncel = odaDeposu.update(oda);
        return RoomResponse.from(guncel);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Room oda = odayiBul(id);

        videoDeposu.deleteByRoomId(id);
        odaDeposu.delete(oda);
    }

    private Room odayiBul(Long id) {
        Optional<Room> bulunan = odaDeposu.findById(id);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Oda", id);
        }
        return bulunan.get();
    }
}
