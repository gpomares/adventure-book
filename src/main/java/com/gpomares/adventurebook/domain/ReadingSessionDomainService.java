package com.gpomares.adventurebook.domain;

public interface ReadingSessionDomainService {

    ReadingSessionState start(Long bookId);

    ReadingSessionState chooseOption(Long bookId, Long sessionId, Long optionId);
}
