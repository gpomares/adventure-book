package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBookDomainService;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationBookServiceImpl implements AdventureBookService {

    private final AdventureBookDomainService adventureBookDomainService;
    private final AdventureBookMapper mapper;

    @Autowired
    public ApplicationBookServiceImpl(AdventureBookDomainService adventureBookDomainService, AdventureBookMapper mapper) {
        this.adventureBookDomainService = adventureBookDomainService;
        this.mapper = mapper;
    }

    @Override
    public AdventureBookSummaryDto get(Long id) {
        return mapper.mapSummary(adventureBookDomainService.findById(id));
    }
}
