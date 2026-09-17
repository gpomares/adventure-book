package com.gpomares.adventurebook.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    Optional<ReadingSession> findByIdAndBookIdAndOwnerId(Long id, Long bookId, Long ownerId);

    Optional<ReadingSession> findByIdAndBookId(Long id, Long bookId);
}
