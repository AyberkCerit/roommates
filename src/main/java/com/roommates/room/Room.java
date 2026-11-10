package com.roommates.room;

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

import com.roommates.user.User;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RoomType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private RoomVisibility visibility;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closes_at")
    private Instant closesAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    protected Room() {
    }

    public Room(String name, String description, RoomType type, RoomVisibility visibility,
                Integer maxMembers, Instant closesAt, User owner, Instant createdAt) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.visibility = visibility;
        this.maxMembers = maxMembers;
        this.closesAt = closesAt;
        this.owner = owner;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public RoomVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(RoomVisibility visibility) {
        this.visibility = visibility;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(Instant closesAt) {
        this.closesAt = closesAt;
    }

    public User getOwner() {
        return owner;
    }
}
