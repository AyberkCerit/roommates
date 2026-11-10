package com.roommates.study.dto;

import java.time.Instant;

import com.roommates.study.StudySession;
import com.roommates.study.StudySessionStatus;

public record StudySessionResponse(
        Long id,
        Long roomId,
        String title,
        Integer focusMinutes,
        Integer breakMinutes,
        Integer rounds,
        StudySessionStatus status,
        Instant startedAt,
        boolean timerActive
) {
    public static StudySessionResponse from(StudySession oturum, boolean zamanlayiciAktif) {
        return new StudySessionResponse(
                oturum.getId(),
                oturum.getRoom().getId(),
                oturum.getTitle(),
                oturum.getFocusMinutes(),
                oturum.getBreakMinutes(),
                oturum.getRounds(),
                oturum.getStatus(),
                oturum.getStartedAt(),
                zamanlayiciAktif);
    }
}
