package com.gpomares.adventurebook.web.mapper;

import com.gpomares.adventurebook.dto.AdventureBookSummaryDto;
import com.gpomares.adventurebook.web.json.AdventureBookSummary;
import org.springframework.stereotype.Component;

@Component
public class AdventureBookJsonMapper {

    public AdventureBookSummary mapSummary(AdventureBookSummaryDto dto) {
        return new AdventureBookSummary(
                dto.id(), dto.title(), dto.author(), dto.difficulty(), dto.categories()
        );
    }
}
