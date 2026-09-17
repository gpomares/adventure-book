package com.gpomares.adventurebook.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reading_sessions")
public class ReadingSession {

    public static final int INITIAL_HEALTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "current_section_number", nullable = false)
    private long currentSectionNumber;

    @Column(nullable = false)
    private int health;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingSessionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Long ownerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    private Long createdByUserId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by_user_id", nullable = false)
    private Long updatedByUserId;

    @Version
    @Column(nullable = false)
    private Long version;

    protected ReadingSession() {
    }

    private ReadingSession(long bookId, long currentSectionNumber, long ownerId) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("bookId must be positive");
        }
        if (currentSectionNumber <= 0) {
            throw new IllegalArgumentException("currentSectionNumber must be positive");
        }
        if (ownerId <= 0) {
            throw new IllegalArgumentException("ownerId must be positive");
        }

        Instant now = Instant.now();
        this.bookId = bookId;
        this.currentSectionNumber = currentSectionNumber;
        this.health = INITIAL_HEALTH;
        this.status = ReadingSessionStatus.IN_PROGRESS;
        this.startedAt = now;
        this.ownerId = ownerId;
        this.createdAt = now;
        this.createdByUserId = ownerId;
        this.updatedAt = now;
        this.updatedByUserId = ownerId;
    }

    public static ReadingSession start(long bookId, long currentSectionNumber, long ownerId) {
        return new ReadingSession(bookId, currentSectionNumber, ownerId);
    }

    public void progressTo(long destinationSectionNumber, int health, ReadingSessionStatus status, long updatedByUserId) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (status == ReadingSessionStatus.IN_PROGRESS && this.status != ReadingSessionStatus.IN_PROGRESS) {
            throw new ReadingSessionConflictException("Reading session is no longer in progress");
        }
        if (destinationSectionNumber <= 0) {
            throw new IllegalArgumentException("destinationSectionNumber must be positive");
        }
        if (health < 0 || health > INITIAL_HEALTH) {
            throw new IllegalArgumentException("health must be between 0 and " + INITIAL_HEALTH);
        }
        if (updatedByUserId <= 0) {
            throw new IllegalArgumentException("updatedByUserId must be positive");
        }

        this.currentSectionNumber = destinationSectionNumber;
        this.health = health;
        this.status = status;
        this.endedAt = status == ReadingSessionStatus.IN_PROGRESS ? null : Instant.now();
        this.updatedAt = Instant.now();
        this.updatedByUserId = updatedByUserId;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public long getCurrentSectionNumber() {
        return currentSectionNumber;
    }

    public int getHealth() {
        return health;
    }

    public ReadingSessionStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public Long getVersion() {
        return version;
    }
}
