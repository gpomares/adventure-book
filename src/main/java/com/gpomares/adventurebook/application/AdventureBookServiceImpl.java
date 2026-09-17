package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBookDomainService;
import com.gpomares.adventurebook.domain.AdventureBookFilter;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdventureBookServiceImpl implements AdventureBookService {

    private final AdventureBookDomainService adventureBookDomainService;
    private final AdventureBookMapper mapper;

    @Autowired
    public AdventureBookServiceImpl(AdventureBookDomainService adventureBookDomainService, AdventureBookMapper mapper) {
        this.adventureBookDomainService = adventureBookDomainService;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AdventureBookSummaryDto get(Long id) {
        return mapper.mapSummary(adventureBookDomainService.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdventureBookSummaryDto> search(AdventureBookFilter filter, Pageable pageable) {
        return adventureBookDomainService.search(filter, pageable).map(mapper::mapSummary);
    }

    @Override
    public boolean addCategory(Long id, String category) {
        return adventureBookDomainService.addCategory(id, category);
    }

    @Override
    public void replaceCategories(Long id, List<String> categories) {
        adventureBookDomainService.replaceCategories(id, categories);
    }

    @Override
    public void removeCategory(Long id, String category) {
        adventureBookDomainService.removeCategory(id, category);
    }
}
