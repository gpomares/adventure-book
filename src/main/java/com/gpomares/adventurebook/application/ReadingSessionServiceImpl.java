package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.ReadingSessionDomainService;
import com.gpomares.adventurebook.dto.ReadingSessionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingSessionServiceImpl implements ReadingSessionService {

    private final ReadingSessionDomainService readingSessionDomainService;
    private final ReadingSessionMapper mapper;

    public ReadingSessionServiceImpl(ReadingSessionDomainService readingSessionDomainService,
                                     ReadingSessionMapper mapper) {
        this.readingSessionDomainService = readingSessionDomainService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReadingSessionDto start(Long bookId) {
        return mapper.map(readingSessionDomainService.start(bookId));
    }

    @Override
    @Transactional
    public ReadingSessionDto chooseOption(Long bookId, Long sessionId, Long optionId) {
        return mapper.map(readingSessionDomainService.chooseOption(bookId, sessionId, optionId));
    }
}
