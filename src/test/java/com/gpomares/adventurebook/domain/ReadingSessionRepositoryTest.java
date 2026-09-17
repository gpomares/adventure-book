package com.gpomares.adventurebook.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReadingSessionRepositoryTest {

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private AdventureBookRepository adventureBookRepository;

    @Autowired
    private EntityManager entityManager;

    private AdventureBook book;

    @BeforeEach
    void setUp() {
        readingSessionRepository.deleteAll();
        adventureBookRepository.deleteAll();

        Section begin = Section.Builder.section()
                .sectionNumber(1)
                .text("The beginning")
                .type(SectionType.BEGIN)
                .build();
        book = adventureBookRepository.saveAndFlush(AdventureBook.Builder.adventureBook()
                .title("The Crystal Caverns")
                .author("Evelyn Stormrider")
                .difficulty(Difficulty.EASY)
                .sections(java.util.List.of(begin))
                .build());
    }

    @Test
    void persistsAStartedSessionAndFindsItWithinItsBook() {
        ReadingSession saved = readingSessionRepository.saveAndFlush(
                ReadingSession.start(book.getId(), 1));
        entityManager.clear();

        ReadingSession found = readingSessionRepository.findByIdAndBookId(saved.getId(), book.getId())
                .orElseThrow();

        assertEquals(book.getId(), found.getBookId());
        assertEquals(1, found.getCurrentSectionNumber());
        assertEquals(ReadingSession.INITIAL_HEALTH, found.getHealth());
        assertEquals(ReadingSessionStatus.IN_PROGRESS, found.getStatus());
        assertNotNull(found.getStartedAt());
        assertNotNull(found.getVersion());
        assertTrue(readingSessionRepository.findByIdAndBookId(saved.getId(), book.getId() + 1).isEmpty());
    }
}
