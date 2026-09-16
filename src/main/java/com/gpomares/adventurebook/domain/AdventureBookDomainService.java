package com.gpomares.adventurebook.domain;

import java.util.List;

public interface AdventureBookDomainService {

    AdventureBook findById(Long id);

    boolean addCategory(Long id, String category);

    void replaceCategories(Long id, List<String> categories);

    void removeCategory(Long id, String category);
}
