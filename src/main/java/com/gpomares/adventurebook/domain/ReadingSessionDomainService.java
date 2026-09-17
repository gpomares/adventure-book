package com.gpomares.adventurebook.domain;

public interface ReadingSessionDomainService {

    ReadingSessionState start(Long bookId, Long ownerId);

    ReadingSessionState chooseOption(Long bookId, Long sessionId, Long optionId, Long updatedByUserId);

}
