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

    @Version
    @Column(nullable = false)
    private Long version;

    protected ReadingSession() {
    }

    private ReadingSession(long bookId, long currentSectionNumber) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("bookId must be positive");
        }
        if (currentSectionNumber <= 0) {
            throw new IllegalArgumentException("currentSectionNumber must be positive");
        }

        this.bookId = bookId;
        this.currentSectionNumber = currentSectionNumber;
        this.health = INITIAL_HEALTH;
        this.status = ReadingSessionStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public static ReadingSession start(long bookId, long currentSectionNumber) {
        return new ReadingSession(bookId, currentSectionNumber);
    }

    public void progressTo(long destinationSectionNumber, int health, ReadingSessionStatus status) {
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

        this.currentSectionNumber = destinationSectionNumber;
        this.health = health;
        this.status = status;
        this.endedAt = status == ReadingSessionStatus.IN_PROGRESS ? null : Instant.now();
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

    public Long getVersion() {
        return version;
    }
}
