package com.gpomares.adventurebook.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdventureBookDomainService {

    AdventureBook findById(Long id);

    Page<AdventureBook> search(AdventureBookFilter filter, Pageable pageable);

    boolean addCategory(Long id, String category);

    void replaceCategories(Long id, List<String> categories);

    void removeCategory(Long id, String category);
}
