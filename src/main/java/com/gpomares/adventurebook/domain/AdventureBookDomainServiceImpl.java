package com.gpomares.adventurebook.domain;

import com.gpomares.adventurebook.exception.AdventureBookNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdventureBookDomainServiceImpl implements AdventureBookDomainService {

    private final AdventureBookRepository adventureBookRepository;

    @Autowired
    public AdventureBookDomainServiceImpl(AdventureBookRepository adventureBookRepository) {
        this.adventureBookRepository = adventureBookRepository;
    }

    @Override
    public AdventureBook findById(Long id) {
        return adventureBookRepository.findById(id).orElseThrow(() -> new AdventureBookNotFoundException(id));
    }
}
