package com.roommates.study;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.roommates.room.Room;

@Entity
@Table(name = "study_sessions")
public class StudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "focus_minutes", nullable = false)
    private Integer focusMinutes;

    @Column(name = "break_minutes", nullable = false)
    private Integer breakMinutes;

    @Column(name = "rounds", nullable = false)
    private Integer rounds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StudySessionStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Room room;

    protected StudySession() {
    }

    public StudySession(String title, Integer focusMinutes, Integer breakMinutes, Integer rounds, Room room) {
        this.title = title;
        this.focusMinutes = focusMinutes;
        this.breakMinutes = breakMinutes;
        this.rounds = rounds;
        this.room = room;
        this.status = StudySessionStatus.PLANNED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getFocusMinutes() {
        return focusMinutes;
    }

    public void setFocusMinutes(Integer focusMinutes) {
        this.focusMinutes = focusMinutes;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(Integer breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    public Integer getRounds() {
        return rounds;
    }

    public void setRounds(Integer rounds) {
        this.rounds = rounds;
    }

    public StudySessionStatus getStatus() {
        return status;
    }

    public void setStatus(StudySessionStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
