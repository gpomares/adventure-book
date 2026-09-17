package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.dto.ReadingSessionDto;

public interface ReadingSessionService {

    ReadingSessionDto start(Long bookId);

    ReadingSessionDto chooseOption(Long bookId, Long sessionId, Long optionId);
}
