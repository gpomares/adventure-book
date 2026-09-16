package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBookDomainService;
import com.gpomares.adventurebook.dto.AdventureBookDto;
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
    public AdventureBookDto get(Long id) {
        return mapper.map(adventureBookDomainService.findById(id));
    }
}
