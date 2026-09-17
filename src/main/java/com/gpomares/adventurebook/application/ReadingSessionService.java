package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.dto.ReadingSessionDto;

public interface ReadingSessionService {

    ReadingSessionDto start(Long bookId, Long userId);

    ReadingSessionDto chooseOption(Long bookId, Long sessionId, Long optionId, Long userId);

    ReadingSessionDto get(Long sessionId, Long userId);

    java.util.List<ReadingSessionDto> list(Long userId, com.gpomares.adventurebook.domain.ReadingSessionStatus status);
}
