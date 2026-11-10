package com.roommates.study;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roommates.common.error.InvalidRequestException;
import com.roommates.common.error.ResourceNotFoundException;
import com.roommates.room.Room;
import com.roommates.room.RoomRepository;
import com.roommates.room.RoomType;
import com.roommates.study.dto.StudySessionCreateRequest;
import com.roommates.study.dto.StudySessionPatchRequest;
import com.roommates.study.dto.StudySessionResponse;
import com.roommates.study.dto.StudySessionUpdateRequest;

@Service
public class StudySessionServiceImpl implements StudySessionService {
    private final StudySessionRepository oturumDeposu;
    private final StudyTimerRegistry zamanlayicilar;
    private final RoomRepository odaDeposu;
    private final Clock saat;
    private final int varsayilanCalismaDakikasi;
    private final int varsayilanMolaDakikasi;

    public StudySessionServiceImpl(StudySessionRepository oturumDeposu,
                                   StudyTimerRegistry zamanlayicilar,
                                   RoomRepository odaDeposu,
                                   Clock saat,
                                   @Value("${roommates.study.default-focus-minutes}") int varsayilanCalismaDakikasi,
                                   @Value("${roommates.study.default-break-minutes}") int varsayilanMolaDakikasi) {
        this.oturumDeposu = oturumDeposu;
        this.zamanlayicilar = zamanlayicilar;
        this.odaDeposu = odaDeposu;
        this.saat = saat;
        this.varsayilanCalismaDakikasi = varsayilanCalismaDakikasi;
        this.varsayilanMolaDakikasi = varsayilanMolaDakikasi;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySessionResponse> findAll(Long roomId, StudySessionStatus status) {
        List<StudySessionResponse> cevaplar = new ArrayList<>();
        for (StudySession oturum : oturumDeposu.findByFilter(roomId, status)) {
            cevaplar.add(cevabaCevir(oturum));
        }
        return cevaplar;
    }

    @Override
    @Transactional(readOnly = true)
    public StudySessionResponse findById(Long id) {
        return cevabaCevir(oturumuBul(id));
    }

    @Override
    @Transactional
    public StudySessionResponse create(StudySessionCreateRequest istek) {
        Room oda = calismaOdasiniBul(istek.roomId());

        int calismaDakikasi = varsayilanCalismaDakikasi;
        if (istek.focusMinutes() != null) {
            calismaDakikasi = istek.focusMinutes();
        }
        int molaDakikasi = varsayilanMolaDakikasi;
        if (istek.breakMinutes() != null) {
            molaDakikasi = istek.breakMinutes();
        }

        StudySession yeniOturum = new StudySession(istek.title(), calismaDakikasi, molaDakikasi,
                istek.rounds(), oda);
        oturumDeposu.save(yeniOturum);

        return cevabaCevir(yeniOturum);
    }

    @Override
    @Transactional
    public StudySessionResponse update(Long id, StudySessionUpdateRequest istek) {
        StudySession oturum = oturumuBul(id);
        Room oda = calismaOdasiniBul(istek.roomId());

        durumuDegistir(oturum, istek.status());
        oturum.setTitle(istek.title());
        oturum.setRoom(oda);
        oturum.setFocusMinutes(istek.focusMinutes());
        oturum.setBreakMinutes(istek.breakMinutes());
        oturum.setRounds(istek.rounds());

        StudySession guncel = oturumDeposu.update(oturum);
        return cevabaCevir(guncel);
    }

    @Override
    @Transactional
    public StudySessionResponse patch(Long id, StudySessionPatchRequest istek) {
        StudySession oturum = oturumuBul(id);

        if (istek.id() != null) {
            throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
        }

        if (istek.roomId() != null) {
            oturum.setRoom(calismaOdasiniBul(istek.roomId()));
        }
        if (istek.status() != null) {
            durumuDegistir(oturum, istek.status());
        }
        if (istek.title() != null) {
            oturum.setTitle(istek.title());
        }
        if (istek.focusMinutes() != null) {
            oturum.setFocusMinutes(istek.focusMinutes());
        }
        if (istek.breakMinutes() != null) {
            oturum.setBreakMinutes(istek.breakMinutes());
        }
        if (istek.rounds() != null) {
            oturum.setRounds(istek.rounds());
        }

        StudySession guncel = oturumDeposu.update(oturum);
        return cevabaCevir(guncel);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StudySession oturum = oturumuBul(id);
        oturumDeposu.delete(oturum);
        zamanlayicilar.unregister(id);
    }

    private void durumuDegistir(StudySession oturum, StudySessionStatus yeniDurum) {
        StudySessionStatus eskiDurum = oturum.getStatus();

        if (!eskiDurum.canTransitionTo(yeniDurum)) {
            throw new InvalidRequestException("Geçersiz durum geçişi: " + eskiDurum + " → " + yeniDurum);
        }

        if (eskiDurum == StudySessionStatus.PLANNED && yeniDurum == StudySessionStatus.RUNNING) {
            Instant simdi = Instant.now(saat);
            oturum.setStartedAt(simdi);
            zamanlayicilar.register(oturum.getId(), simdi);
        }
        if (eskiDurum == StudySessionStatus.RUNNING && yeniDurum == StudySessionStatus.FINISHED) {
            zamanlayicilar.unregister(oturum.getId());
        }

        oturum.setStatus(yeniDurum);
    }

    private StudySession oturumuBul(Long id) {
        Optional<StudySession> bulunan = oturumDeposu.findById(id);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Çalışma oturumu", id);
        }
        return bulunan.get();
    }

    private Room calismaOdasiniBul(Long roomId) {
        Optional<Room> bulunan = odaDeposu.findById(roomId);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Oda", roomId);
        }
        Room oda = bulunan.get();
        if (oda.getType() != RoomType.STUDY) {
            throw new InvalidRequestException("Çalışma oturumu yalnız çalışma odasında açılabilir");
        }
        return oda;
    }

    private StudySessionResponse cevabaCevir(StudySession oturum) {
        boolean zamanlayiciAktif = zamanlayicilar.isActive(oturum.getId());
        return StudySessionResponse.from(oturum, zamanlayiciAktif);
    }
}
