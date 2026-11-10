package com.roommates.study;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StudyTimerRegistry {
    private static final Logger logYazici = LoggerFactory.getLogger(StudyTimerRegistry.class);

    private final StudySessionRepository oturumDeposu;
    private final Map<Long, Instant> calisanZamanlayicilar = new ConcurrentHashMap<>();

    public StudyTimerRegistry(StudySessionRepository oturumDeposu) {
        this.oturumDeposu = oturumDeposu;
    }

    @PostConstruct
    public void init() {
        List<StudySession> calisanOturumlar = oturumDeposu.findByStatus(StudySessionStatus.RUNNING);
        for (StudySession oturum : calisanOturumlar) {
            Instant baslangic = oturum.getStartedAt();
            if (baslangic == null) {
                baslangic = Instant.EPOCH;
            }
            calisanZamanlayicilar.put(oturum.getId(), baslangic);
        }
        logYazici.info("StudyTimerRegistry: " + calisanZamanlayicilar.size() + " çalışan oturum yüklendi");
    }

    @PreDestroy
    public void shutdown() {
        int sayi = calisanZamanlayicilar.size();
        calisanZamanlayicilar.clear();
        logYazici.info("StudyTimerRegistry: " + sayi + " zamanlayıcı kapatıldı");
    }

    public void register(Long sessionId, Instant startedAt) {
        calisanZamanlayicilar.put(sessionId, startedAt);
    }

    public void unregister(Long sessionId) {
        calisanZamanlayicilar.remove(sessionId);
    }

    public boolean isActive(Long sessionId) {
        return calisanZamanlayicilar.containsKey(sessionId);
    }

    public int activeCount() {
        return calisanZamanlayicilar.size();
    }
}
