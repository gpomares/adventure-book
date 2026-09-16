package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;

import java.util.List;

public interface AdventureBookService {

    AdventureBookSummaryDto get(Long id);

    boolean addCategory(Long id, String category);

    void replaceCategories(Long id, List<String> categories);

    void removeCategory(Long id, String category);
}
