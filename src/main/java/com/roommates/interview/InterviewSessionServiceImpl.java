package com.roommates.interview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roommates.common.error.InvalidRequestException;
import com.roommates.common.error.ResourceNotFoundException;
import com.roommates.interview.dto.InterviewSessionCreateRequest;
import com.roommates.interview.dto.InterviewSessionPatchRequest;
import com.roommates.interview.dto.InterviewSessionResponse;
import com.roommates.interview.dto.InterviewSessionUpdateRequest;
import com.roommates.room.Room;
import com.roommates.room.RoomRepository;
import com.roommates.room.RoomType;
import com.roommates.user.User;
import com.roommates.user.UserRepository;

@Service
public class InterviewSessionServiceImpl implements InterviewSessionService {
    private final InterviewSessionRepository mulakatDeposu;
    private final RoomRepository odaDeposu;
    private final UserRepository kullaniciDeposu;
    private final QuestionProvider teknikSoruSaglayici;
    private final QuestionProvider davranissalSoruSaglayici;

    public InterviewSessionServiceImpl(InterviewSessionRepository mulakatDeposu,
                                       RoomRepository odaDeposu,
                                       UserRepository kullaniciDeposu,
                                       QuestionProvider technicalQuestionProvider,
                                       @Qualifier("behavioral") @Lazy QuestionProvider behavioralQuestionProvider) {
        this.mulakatDeposu = mulakatDeposu;
        this.odaDeposu = odaDeposu;
        this.kullaniciDeposu = kullaniciDeposu;
        this.teknikSoruSaglayici = technicalQuestionProvider;
        this.davranissalSoruSaglayici = behavioralQuestionProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewSessionResponse> findAll(InterviewStatus status) {
        List<InterviewSessionResponse> cevaplar = new ArrayList<>();
        for (InterviewSession mulakat : mulakatDeposu.findByStatus(status)) {
            cevaplar.add(InterviewSessionResponse.from(mulakat));
        }
        return cevaplar;
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewSessionResponse findById(Long id) {
        return InterviewSessionResponse.from(mulakatiBul(id));
    }

    @Override
    @Transactional
    public InterviewSessionResponse create(InterviewSessionCreateRequest istek) {
        Room oda = mulakatOdasiniBul(istek.roomId());
        User mulakatci = kullaniciyiBul(istek.interviewerId());
        User aday = kullaniciyiBul(istek.candidateId());
        ayniKisiOlamaz(mulakatci, aday);

        String soru = soruSec(istek.category(), istek.topic());

        InterviewSession yeniMulakat = new InterviewSession(istek.topic(), istek.category(), soru,
                InterviewStatus.SCHEDULED, istek.scheduledAt(), oda, mulakatci, aday);
        mulakatDeposu.save(yeniMulakat);

        return InterviewSessionResponse.from(yeniMulakat);
    }

    @Override
    @Transactional
    public InterviewSessionResponse update(Long id, InterviewSessionUpdateRequest istek) {
        InterviewSession mulakat = mulakatiBul(id);
        Room oda = mulakatOdasiniBul(istek.roomId());
        User mulakatci = kullaniciyiBul(istek.interviewerId());
        User aday = kullaniciyiBul(istek.candidateId());
        ayniKisiOlamaz(mulakatci, aday);

        mulakat.setRoom(oda);
        mulakat.setInterviewer(mulakatci);
        mulakat.setCandidate(aday);
        mulakat.setTopic(istek.topic());
        mulakat.setCategory(istek.category());
        mulakat.setQuestion(istek.question());
        mulakat.setStatus(istek.status());
        mulakat.setScheduledAt(istek.scheduledAt());

        InterviewSession guncel = mulakatDeposu.update(mulakat);
        return InterviewSessionResponse.from(guncel);
    }

    @Override
    @Transactional
    public InterviewSessionResponse patch(Long id, InterviewSessionPatchRequest istek) {
        InterviewSession mulakat = mulakatiBul(id);

        if (istek.id() != null) {
            throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
        }

        if (istek.roomId() != null) {
            mulakat.setRoom(mulakatOdasiniBul(istek.roomId()));
        }
        if (istek.interviewerId() != null) {
            mulakat.setInterviewer(kullaniciyiBul(istek.interviewerId()));
        }
        if (istek.candidateId() != null) {
            mulakat.setCandidate(kullaniciyiBul(istek.candidateId()));
        }
        ayniKisiOlamaz(mulakat.getInterviewer(), mulakat.getCandidate());

        if (istek.topic() != null) {
            mulakat.setTopic(istek.topic());
        }
        if (istek.category() != null) {
            mulakat.setCategory(istek.category());
        }
        if (istek.question() != null) {
            mulakat.setQuestion(istek.question());
        }
        if (istek.status() != null) {
            mulakat.setStatus(istek.status());
        }
        if (istek.scheduledAt() != null) {
            mulakat.setScheduledAt(istek.scheduledAt());
        }

        InterviewSession guncel = mulakatDeposu.update(mulakat);
        return InterviewSessionResponse.from(guncel);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        InterviewSession mulakat = mulakatiBul(id);
        mulakatDeposu.delete(mulakat);
    }

    private String soruSec(InterviewCategory kategori, String konu) {
        if (kategori == InterviewCategory.BEHAVIORAL) {
            return davranissalSoruSaglayici.nextQuestion(konu);
        }
        return teknikSoruSaglayici.nextQuestion(konu);
    }

    private void ayniKisiOlamaz(User mulakatci, User aday) {
        if (mulakatci.getId().equals(aday.getId())) {
            throw new InvalidRequestException("Mülakatçı ve aday aynı kişi olamaz");
        }
    }

    private InterviewSession mulakatiBul(Long id) {
        Optional<InterviewSession> bulunan = mulakatDeposu.findById(id);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Mülakat oturumu", id);
        }
        return bulunan.get();
    }

    private Room mulakatOdasiniBul(Long roomId) {
        Optional<Room> bulunan = odaDeposu.findById(roomId);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Oda", roomId);
        }
        Room oda = bulunan.get();
        if (oda.getType() != RoomType.INTERVIEW) {
            throw new InvalidRequestException("Mülakat oturumu yalnız mülakat odasında açılabilir");
        }
        return oda;
    }

    private User kullaniciyiBul(Long userId) {
        Optional<User> bulunan = kullaniciDeposu.findById(userId);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Kullanıcı", userId);
        }
        return bulunan.get();
    }
}
