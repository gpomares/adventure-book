package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;

public interface AdventureBookService {

    AdventureBookSummaryDto get(Long id);
}
