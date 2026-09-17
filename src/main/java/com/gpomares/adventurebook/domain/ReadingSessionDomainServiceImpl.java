package com.gpomares.adventurebook.domain;

import com.gpomares.adventurebook.exception.InvalidAdventureBookException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingSessionDomainServiceImpl implements ReadingSessionDomainService {

    private final AdventureBookDomainService adventureBookDomainService;
    private final ReadingSessionRepository readingSessionRepository;

    public ReadingSessionDomainServiceImpl(AdventureBookDomainService adventureBookDomainService,
                                           ReadingSessionRepository readingSessionRepository) {
        this.adventureBookDomainService = adventureBookDomainService;
        this.readingSessionRepository = readingSessionRepository;
    }

    @Override
    @Transactional
    public ReadingSessionState start(Long bookId, Long ownerId) {
        AdventureBook book = adventureBookDomainService.findById(bookId);
        Section begin = book.getSections().stream()
                .filter(section -> section.getType() == SectionType.BEGIN)
                .findFirst()
                .orElseThrow(() -> new InvalidAdventureBookException("Adventure book has no BEGIN section"));

        ReadingSession session = readingSessionRepository.save(
                ReadingSession.start(book.getId(), begin.getSectionNumber(), ownerId));
        return new ReadingSessionState(session, begin, null);
    }

    @Override
    @Transactional
    public ReadingSessionState chooseOption(Long bookId, Long sessionId, Long optionId, Long updatedByUserId) {
        AdventureBook book = adventureBookDomainService.findById(bookId);
        ReadingSession session = readingSessionRepository.findByIdAndBookIdAndOwnerId(sessionId, bookId, updatedByUserId)
                .orElseThrow(() -> new ReadingSessionNotFoundException(sessionId));
        if (session.getStatus() != ReadingSessionStatus.IN_PROGRESS) {
            throw new ReadingSessionConflictException("Reading session is no longer in progress");
        }

        Section currentSection = findSection(book, session.getCurrentSectionNumber());
        Option option = currentSection.getOptions().stream()
                .filter(candidate -> optionId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new ReadingSessionConflictException(
                        "Option is not available from the current section"));

        int health = applyConsequence(session.getHealth(), option.getConsequence());
        Section destination = findSection(book, option.getGotoId());
        session.progressTo(destination.getSectionNumber(), health, statusAfter(health, destination), updatedByUserId);

        return new ReadingSessionState(session, destination, option.getConsequence());
    }

    private Section findSection(AdventureBook book, long sectionNumber) {
        return book.getSections().stream()
                .filter(section -> section.getSectionNumber() == sectionNumber)
                .findFirst()
                .orElseThrow(() -> new InvalidAdventureBookException(
                        "Adventure book has no section with number %d".formatted(sectionNumber)));
    }

    private int applyConsequence(int currentHealth, Consequence consequence) {
        if (consequence == null) {
            return currentHealth;
        }
        if (consequence.getType() == null) {
            throw new InvalidAdventureBookException("Option consequence must have a type");
        }

        return switch (consequence.getType()) {
            case LOSE_HEALTH -> Math.max(0, currentHealth - consequence.getValue());
            case GAIN_HEALTH -> Math.min(ReadingSession.INITIAL_HEALTH, currentHealth + consequence.getValue());
        };
    }

    private ReadingSessionStatus statusAfter(int health, Section destination) {
        if (health == 0) {
            return ReadingSessionStatus.DEAD;
        }
        if (destination.getType() == SectionType.END) {
            return ReadingSessionStatus.COMPLETED;
        }
        if (destination.getOptions().isEmpty()) {
            return ReadingSessionStatus.STUCK;
        }
        return ReadingSessionStatus.IN_PROGRESS;
    }
}
