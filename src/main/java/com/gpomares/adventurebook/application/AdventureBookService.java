package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.domain.AdventureBookFilter;
import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdventureBookService {

    AdventureBookSummaryDto get(Long id);

    Page<AdventureBookSummaryDto> search(AdventureBookFilter filter, Pageable pageable);

    boolean addCategory(Long id, String category);

    void replaceCategories(Long id, List<String> categories);

    void removeCategory(Long id, String category);
}
