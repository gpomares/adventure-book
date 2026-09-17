package com.gpomares.adventurebook.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReadingSessionDomainServiceTest {

    @Autowired
    private ReadingSessionDomainService readingSessionDomainService;

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private AdventureBookRepository adventureBookRepository;

    private AdventureBook book;

    @BeforeEach
    void setUp() {
        readingSessionRepository.deleteAll();
        adventureBookRepository.deleteAll();

        Option enterCrevice = Option.Builder.option().description("Enter the crevice")
                .gotoId(2)
                .consequence(Consequence.Builder.consequence().type(ConsequenceType.LOSE_HEALTH)
                        .value(4).text("You scrape your shoulder.").build())
                .build();
        Option takeDeadEnd = Option.Builder.option().description("Take the dead end")
                .gotoId(3).build();
        Option takeFatalJump = Option.Builder.option().description("Take the fatal jump")
                .gotoId(4)
                .consequence(Consequence.Builder.consequence().type(ConsequenceType.LOSE_HEALTH)
                        .value(10).text("The fall is fatal.").build())
                .build();
        Option rest = Option.Builder.option().description("Rest")
                .gotoId(5)
                .consequence(Consequence.Builder.consequence().type(ConsequenceType.GAIN_HEALTH)
                        .value(8).text("You feel restored.").build())
                .build();

        book = adventureBookRepository.saveAndFlush(AdventureBook.Builder.adventureBook()
                .title("The Crystal Caverns")
                .author("Evelyn Stormrider")
                .difficulty(Difficulty.EASY)
                .sections(List.of(
                        Section.Builder.section().sectionNumber(1).text("Entrance").type(SectionType.BEGIN)
                                .options(List.of(enterCrevice, takeDeadEnd, takeFatalJump)).build(),
                        Section.Builder.section().sectionNumber(2).text("Crevice").type(SectionType.NODE)
                                .options(List.of(rest)).build(),
                        Section.Builder.section().sectionNumber(3).text("Dead end").type(SectionType.NODE).build(),
                        Section.Builder.section().sectionNumber(4).text("Fatal ending").type(SectionType.END).build(),
                        Section.Builder.section().sectionNumber(5).text("Safe ending").type(SectionType.END).build()))
                .build());
    }

    @Test
    void startsAtTheBeginSectionWithInitialHealth() {
        ReadingSessionState state = readingSessionDomainService.start(book.getId());

        assertNotNull(state.session().getId());
        assertEquals(ReadingSession.INITIAL_HEALTH, state.session().getHealth());
        assertEquals(ReadingSessionStatus.IN_PROGRESS, state.session().getStatus());
        assertEquals(1, state.section().getSectionNumber());
        assertEquals(SectionType.BEGIN, state.section().getType());
        assertEquals(null, state.consequence());
    }

    @Test
    void appliesConsequencesMovesToTheDestinationAndCompletesTheSession() {
        ReadingSessionState started = readingSessionDomainService.start(book.getId());

        ReadingSessionState afterInjury = readingSessionDomainService.chooseOption(
                book.getId(), started.session().getId(), optionId(1, "Enter the crevice"));

        assertEquals(6, afterInjury.session().getHealth());
        assertEquals(ReadingSessionStatus.IN_PROGRESS, afterInjury.session().getStatus());
        assertEquals(2, afterInjury.section().getSectionNumber());
        assertEquals(ConsequenceType.LOSE_HEALTH, afterInjury.consequence().getType());

        ReadingSessionState completed = readingSessionDomainService.chooseOption(
                book.getId(), started.session().getId(), optionId(2, "Rest"));

        assertEquals(10, completed.session().getHealth());
        assertEquals(ReadingSessionStatus.COMPLETED, completed.session().getStatus());
        assertEquals(5, completed.section().getSectionNumber());
        assertNotNull(completed.session().getEndedAt());
    }

    @Test
    void marksAnAliveReaderAtANonEndSectionWithoutOptionsAsStuck() {
        ReadingSessionState started = readingSessionDomainService.start(book.getId());

        ReadingSessionState stuck = readingSessionDomainService.chooseOption(
                book.getId(), started.session().getId(), optionId(1, "Take the dead end"));

        assertEquals(10, stuck.session().getHealth());
        assertEquals(ReadingSessionStatus.STUCK, stuck.session().getStatus());
        assertEquals(3, stuck.section().getSectionNumber());
        assertNotNull(stuck.session().getEndedAt());
    }

    @Test
    void marksZeroHealthAsDeadAndRejectsAnyLaterChoice() {
        ReadingSessionState started = readingSessionDomainService.start(book.getId());

        ReadingSessionState dead = readingSessionDomainService.chooseOption(
                book.getId(), started.session().getId(), optionId(1, "Take the fatal jump"));

        assertEquals(0, dead.session().getHealth());
        assertEquals(ReadingSessionStatus.DEAD, dead.session().getStatus());
        assertThrows(ReadingSessionConflictException.class, () -> readingSessionDomainService.chooseOption(
                book.getId(), started.session().getId(), optionId(1, "Enter the crevice")));
    }

    @Test
    void rejectsAnOptionThatIsNotAvailableFromTheCurrentSection() {
        ReadingSessionState started = readingSessionDomainService.start(book.getId());

        assertThrows(ReadingSessionConflictException.class, () -> readingSessionDomainService.chooseOption(
                book.getId(), started.session().getId(), optionId(2, "Rest")));
    }

    private long optionId(long sectionNumber, String description) {
        return book.getSections().stream()
                .filter(section -> section.getSectionNumber() == sectionNumber)
                .flatMap(section -> section.getOptions().stream())
                .filter(option -> option.getDescription().equals(description))
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
