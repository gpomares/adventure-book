package com.gpomares.adventurebook.web.mapper;

import com.gpomares.adventurebook.dto.AdventureBookDto;
import com.gpomares.adventurebook.web.json.AdventureBook;
import org.springframework.stereotype.Component;

@Component
public class AdventureBookJsonMapper {

    public AdventureBook map(AdventureBookDto dto) {
        return new AdventureBook(
                dto.id(), dto.title(), dto.author(), dto.difficulty(), dto.categories()
        );
    }
}
