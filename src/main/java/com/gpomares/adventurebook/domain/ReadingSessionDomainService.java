package com.gpomares.adventurebook.domain;

public interface ReadingSessionDomainService {

    ReadingSessionState start(Long bookId, Long ownerId);

    ReadingSessionState chooseOption(Long bookId, Long sessionId, Long optionId, Long updatedByUserId);

    ReadingSessionState get(Long sessionId, Long ownerId);

    java.util.List<ReadingSessionState> list(Long ownerId, ReadingSessionStatus status);

}
